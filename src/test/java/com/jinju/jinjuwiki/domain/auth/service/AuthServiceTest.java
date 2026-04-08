package com.jinju.jinjuwiki.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationSendRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationVerifyRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.LoginRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.LoginResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.SignupRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.SignupResponse;
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
class AuthServiceTest {

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
    @DisplayName("회원가입에 성공하면 사용자 기본 정보를 반환한다.")
    void signupSuccess() {
        // given
        verifyEmail("user1@test.com");
        SignupRequest request = new SignupRequest("user1@test.com", "password123", "user1");

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertThat(response.userId()).isNotNull();
        assertThat(response.email()).isEqualTo("user1@test.com");
        assertThat(response.nickname()).isEqualTo("user1");
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입하면 예외가 발생한다.")
    void signupDuplicateEmail() {
        // given
        verifyEmail("dup@test.com");
        authService.signup(new SignupRequest("dup@test.com", "password123", "user1"));

        // when
        Throwable thrown = catchThrowable(
                () -> authService.signup(new SignupRequest("dup@test.com", "password123", "user2"))
        );

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("로그인에 성공하면 사용자 정보를 반환한다.")
    void loginSuccess() {
        // given
        verifyEmail("login@test.com");
        authService.signup(new SignupRequest("login@test.com", "password123", "loginUser"));

        // when
        LoginResponse response = authService.login(new LoginRequest("login@test.com", "password123"));

        // then
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.email()).isEqualTo("login@test.com");
        assertThat(response.nickname()).isEqualTo("loginUser");
        assertThat(response.role()).isEqualTo("USER");
    }

    @Test
    @DisplayName("비밀번호가 다르면 로그인에 실패한다.")
    void loginFailWithInvalidPassword() {
        // given
        verifyEmail("wrong@test.com");
        authService.signup(new SignupRequest("wrong@test.com", "password123", "wrongUser"));

        // when
        Throwable thrown = catchThrowable(
                () -> authService.login(new LoginRequest("wrong@test.com", "bad-password"))
        );

        // then
        assertThat(thrown)
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_LOGIN);
    }

    private void verifyEmail(String email) {
        authService.sendVerificationCode(new EmailVerificationSendRequest(email));
        String code = emailVerificationRepository.findByEmail(email).orElseThrow().getCode();
        authService.verifyCode(new EmailVerificationVerifyRequest(email, code));
    }
}
