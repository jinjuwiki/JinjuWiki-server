package com.jinju.jinjuwiki.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationSendRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationVerifyRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.SignupRequest;
import com.jinju.jinjuwiki.domain.auth.repository.EmailVerificationRepository;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class EmailVerificationServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @TestConfiguration
    static class TestMailConfig {

        @Bean
        @Primary
        EmailSender emailSender() {
            return (to, code) -> {
            };
        }
    }

    @Test
    @DisplayName("이메일 인증이 완료되지 않으면 회원가입할 수 없다.")
    void signupFailWhenEmailNotVerified() {
        // given
        SignupRequest request = new SignupRequest("unverified@test.com", "password123", "user3");

        // when
        Throwable thrown = catchThrowable(() -> authService.signup(request));

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);
    }

    @Test
    @DisplayName("인증코드 검증에 성공하면 인증 완료 상태가 된다.")
    void verifyCodeSuccess() {
        // given
        authService.sendVerificationCode(new EmailVerificationSendRequest("verify@test.com"));
        String code = emailVerificationRepository.findByEmail("verify@test.com").orElseThrow().getCode();

        // when
        var response = authService.verifyCode(new EmailVerificationVerifyRequest("verify@test.com", code));

        // then
        assertThat(response.verified()).isTrue();
        assertThat(response.email()).isEqualTo("verify@test.com");
    }
}
