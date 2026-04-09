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

// 이메일 인증 흐름 단위 테스트 클래스
@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private AuthValidationService authValidationService;

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
        // stubbing
        when(emailVerificationRepository.findByEmail("verify@test.com")).thenReturn(Optional.of(verification));

        // when
        var response = authService.verifyCode(new EmailVerificationVerifyRequest("verify@test.com", "123456"));

        // then
        assertThat(response.verified()).isTrue();
        assertThat(response.email()).isEqualTo("verify@test.com");
        assertThat(response.verifiedAt()).isNotNull();
        assertThat(verification.isVerified()).isTrue();
        // stubbing
        verify(emailVerificationRepository).findByEmail("verify@test.com");
    }
}
