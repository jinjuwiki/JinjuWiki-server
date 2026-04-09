package com.jinju.jinjuwiki.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.jinju.jinjuwiki.domain.auth.entity.EmailVerification;
import com.jinju.jinjuwiki.domain.auth.repository.EmailVerificationRepository;
import com.jinju.jinjuwiki.domain.user.repository.UserRepository;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// 인증 검증 서비스 단위 테스트 클래스
@ExtendWith(MockitoExtension.class)
class AuthValidationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @InjectMocks
    private AuthValidationService authValidationService;

    @Test
    @DisplayName("인증이 완료된 이메일은 만료 시간이 지나도 회원가입 검증을 통과한다.")
    void validateEmailVerifiedSuccessWhenAlreadyVerified() {
        // given
        EmailVerification verification = EmailVerification.builder()
                .email("verified@test.com")
                .code("123456")
                .verified(true)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .verifiedAt(LocalDateTime.now().minusMinutes(2))
                .build();
        when(emailVerificationRepository.findByEmail("verified@test.com")).thenReturn(Optional.of(verification));

        // when & then
        assertThatNoException().isThrownBy(() -> authValidationService.validateEmailVerified("verified@test.com"));
    }

    @Test
    @DisplayName("인증이 완료되지 않은 이메일은 만료 시간이 지나면 예외가 발생한다.")
    void validateEmailVerifiedFailWhenExpiredBeforeVerify() {
        // given
        EmailVerification verification = EmailVerification.builder()
                .email("expired@test.com")
                .code("123456")
                .verified(false)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();
        when(emailVerificationRepository.findByEmail("expired@test.com")).thenReturn(Optional.of(verification));

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authValidationService.validateEmailVerified("expired@test.com")
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
    }
}
