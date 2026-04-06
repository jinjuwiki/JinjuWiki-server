package com.jinju.jinjuwiki.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jinju.jinjuwiki.domain.auth.dto.EmailVerificationSendRequest;
import com.jinju.jinjuwiki.domain.auth.dto.EmailVerificationVerifyRequest;
import com.jinju.jinjuwiki.domain.auth.dto.LoginRequest;
import com.jinju.jinjuwiki.domain.auth.dto.LoginResponse;
import com.jinju.jinjuwiki.domain.auth.dto.SignupRequest;
import com.jinju.jinjuwiki.domain.auth.dto.SignupResponse;
import com.jinju.jinjuwiki.domain.auth.repository.EmailVerificationRepository;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import com.jinju.jinjuwiki.domain.user.entity.User;
import com.jinju.jinjuwiki.domain.user.entity.UserRole;
import com.jinju.jinjuwiki.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

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
        verifyEmail("user1@test.com");
        SignupRequest request = new SignupRequest("user1@test.com", "password123", "user1");

        SignupResponse response = authService.signup(request);

        assertThat(response.userId()).isNotNull();
        assertThat(response.email()).isEqualTo("user1@test.com");
        assertThat(response.nickname()).isEqualTo("user1");
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입하면 예외가 발생한다.")
    void signupDuplicateEmail() {
        verifyEmail("dup@test.com");
        authService.signup(new SignupRequest("dup@test.com", "password123", "user1"));

        assertThatThrownBy(() -> authService.signup(new SignupRequest("dup@test.com", "password123", "user2")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("로그인에 성공하면 사용자 정보를 반환한다.")
    void loginSuccess() {
        verifyEmail("login@test.com");
        authService.signup(new SignupRequest("login@test.com", "password123", "loginUser"));

        LoginResponse response = authService.login(new LoginRequest("login@test.com", "password123"));

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.email()).isEqualTo("login@test.com");
        assertThat(response.nickname()).isEqualTo("loginUser");
        assertThat(response.role()).isEqualTo("USER");
    }

    @Test
    @DisplayName("비밀번호가 다르면 로그인에 실패한다.")
    void loginFailWithInvalidPassword() {
        verifyEmail("wrong@test.com");
        authService.signup(new SignupRequest("wrong@test.com", "password123", "wrongUser"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("wrong@test.com", "bad-password")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_LOGIN);
    }

    @Test
    @DisplayName("DB unique 제약으로 이메일이 중복되어도 데이터 무결성 예외가 발생한다.")
    void duplicateEmailCanFailAtDatabaseLevel() {
        userRepository.save(User.builder()
                .email("dbdup@test.com")
                .password("encoded-password")
                .nickname("dbUser1")
                .role(UserRole.USER)
                .build());

        assertThatThrownBy(() -> userRepository.saveAndFlush(User.builder()
                .email("dbdup@test.com")
                .password("encoded-password")
                .nickname("dbUser2")
                .role(UserRole.USER)
                .build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("이메일 인증이 완료되지 않으면 회원가입할 수 없다.")
    void signupFailWhenEmailNotVerified() {
        assertThatThrownBy(() -> authService.signup(new SignupRequest("unverified@test.com", "password123", "user3")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EMAIL_NOT_VERIFIED);
    }

    @Test
    @DisplayName("인증코드 검증에 성공하면 인증 완료 상태가 된다.")
    void verifyCodeSuccess() {
        authService.sendVerificationCode(new EmailVerificationSendRequest("verify@test.com"));
        String code = emailVerificationRepository.findByEmail("verify@test.com").orElseThrow().getCode();

        var response = authService.verifyCode(new EmailVerificationVerifyRequest("verify@test.com", code));

        assertThat(response.verified()).isTrue();
        assertThat(response.email()).isEqualTo("verify@test.com");
    }

    private void verifyEmail(String email) {
        authService.sendVerificationCode(new EmailVerificationSendRequest(email));
        String code = emailVerificationRepository.findByEmail(email).orElseThrow().getCode();
        authService.verifyCode(new EmailVerificationVerifyRequest(email, code));
    }
}
