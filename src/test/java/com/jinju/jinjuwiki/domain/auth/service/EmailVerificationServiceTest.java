package com.jinju.jinjuwiki.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationVerifyRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.SignupRequest;
import com.jinju.jinjuwiki.domain.auth.entity.EmailVerification;
import com.jinju.jinjuwiki.domain.auth.repository.EmailVerificationRepository;
import com.jinju.jinjuwiki.domain.user.repository.UserRepository;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import com.jinju.jinjuwiki.global.security.JwtTokenProvider;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

// 이메일 인증 흐름 Mockito 단위 테스트 클래스
@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private AuthValidationService authValidationService;

    @Mock
    private RedisEmailVerificationSendRateLimiter redisEmailVerificationSendRateLimiter;

    @Mock
    private RedisPasswordResetRequestRateLimiter redisPasswordResetRequestRateLimiter;

    @Mock
    private RedisEmailVerificationVerifyAttemptLimiter redisEmailVerificationVerifyAttemptLimiter;

    @Mock
    private RedisLoginAttemptLimiter redisLoginAttemptLimiter;

    @Mock
    private RedisPasswordResetVerifyAttemptLimiter redisPasswordResetVerifyAttemptLimiter;

    @Mock
    private EmailSender emailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("이메일 인증이 완료되지 않으면 회원가입할 수 없다.")
    void signupFailWhenEmailNotVerified() {
        // given
        SignupRequest request = new SignupRequest("unverified@test.com", "password123", "user3");
        doNothing().when(authValidationService).validateDuplicateSignup("unverified@test.com", "user3");
        doThrow(new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED))
                .when(authValidationService)
                .validateEmailVerified("unverified@test.com");

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> authService.signup(request));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);
        verify(authValidationService).validateDuplicateSignup("unverified@test.com", "user3");
        verify(authValidationService).validateEmailVerified("unverified@test.com");
    }

    @Test
    @DisplayName("인증코드 검증에 성공하면 인증 완료 상태가 된다.")
    void verifyCodeSuccess() {
        // given
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
        EmailVerification verification = EmailVerification.builder()
                .email("verify@test.com")
                .code("123456")
                .verified(false)
                .expiresAt(expiresAt)
                .build();
        when(emailVerificationRepository.findByEmail("verify@test.com")).thenReturn(Optional.of(verification));

        // when
        var response = authService.verifyCode(new EmailVerificationVerifyRequest("verify@test.com", "123456"));

        // then
        assertThat(response.verified()).isTrue();
        assertThat(response.email()).isEqualTo("verify@test.com");
        assertThat(response.verifiedAt()).isNotNull();
        assertThat(verification.isVerified()).isTrue();
        verify(redisEmailVerificationVerifyAttemptLimiter).validateAllowed("verify@test.com");
        verify(redisEmailVerificationVerifyAttemptLimiter).reset("verify@test.com");
        verify(emailVerificationRepository).findByEmail("verify@test.com");
    }

    @Test
    @DisplayName("인증코드가 틀리면 실패 횟수를 누적한다.")
    void verifyCodeFailWhenCodeMismatch() {
        // given
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
        EmailVerification verification = EmailVerification.builder()
                .email("verify@test.com")
                .code("123456")
                .verified(false)
                .expiresAt(expiresAt)
                .build();
        when(emailVerificationRepository.findByEmail("verify@test.com")).thenReturn(Optional.of(verification));
        when(redisEmailVerificationVerifyAttemptLimiter.recordFailure("verify@test.com")).thenReturn(1L);

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.verifyCode(new EmailVerificationVerifyRequest("verify@test.com", "000000"))
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        verify(redisEmailVerificationVerifyAttemptLimiter).validateAllowed("verify@test.com");
        verify(redisEmailVerificationVerifyAttemptLimiter).recordFailure("verify@test.com");
    }

    @Test
    @DisplayName("인증코드 실패 횟수 제한을 넘으면 차단한다.")
    void verifyCodeFailWhenVerifyAttemptLimitExceeded() {
        // given
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
        EmailVerification verification = EmailVerification.builder()
                .email("verify@test.com")
                .code("123456")
                .verified(false)
                .expiresAt(expiresAt)
                .build();
        when(emailVerificationRepository.findByEmail("verify@test.com")).thenReturn(Optional.of(verification));
        when(redisEmailVerificationVerifyAttemptLimiter.recordFailure("verify@test.com")).thenReturn(5L);

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.verifyCode(new EmailVerificationVerifyRequest("verify@test.com", "000000"))
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMAIL_VERIFICATION_ATTEMPT_EXCEEDED);
        verify(redisEmailVerificationVerifyAttemptLimiter).validateAllowed("verify@test.com");
        verify(redisEmailVerificationVerifyAttemptLimiter).recordFailure("verify@test.com");
    }

    @Test
    @DisplayName("인증코드 검증 요청이 이미 제한 상태면 전용 에러 코드로 응답한다.")
    void verifyCodeFailWhenVerifyAttemptAlreadyBlocked() {
        // given
        doThrow(new BusinessException(ErrorCode.INVALID_INPUT))
                .when(redisEmailVerificationVerifyAttemptLimiter)
                .validateAllowed("verify@test.com");

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.verifyCode(new EmailVerificationVerifyRequest("verify@test.com", "000000"))
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMAIL_VERIFICATION_ATTEMPT_EXCEEDED);
        verify(redisEmailVerificationVerifyAttemptLimiter).validateAllowed("verify@test.com");
    }
}
