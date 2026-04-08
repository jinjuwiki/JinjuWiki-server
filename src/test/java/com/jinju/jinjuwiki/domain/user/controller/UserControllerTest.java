package com.jinju.jinjuwiki.domain.user.controller;

import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationSendRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationVerifyRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.LoginRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.SignupRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.LoginResponse;
import com.jinju.jinjuwiki.domain.auth.repository.EmailVerificationRepository;
import com.jinju.jinjuwiki.domain.auth.service.AuthService;
import com.jinju.jinjuwiki.domain.auth.service.EmailSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @LocalServerPort
    private int port;

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
    @DisplayName("로그인한 사용자는 자신의 프로필을 조회할 수 있다.")
    void getMyProfileSuccess() throws IOException, InterruptedException {
        // given
        verifyEmail("profile@test.com");
        authService.signup(new SignupRequest("profile@test.com", "password123", "profileUser"));
        LoginResponse loginResponse = authService.login(new LoginRequest("profile@test.com", "password123"));

        // when
        HttpResponse<String> response = sendProfileRequest("Bearer " + loginResponse.accessToken());

        // then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"email\":\"profile@test.com\"");
        assertThat(response.body()).contains("\"nickname\":\"profileUser\"");
        assertThat(response.body()).contains("\"role\":\"USER\"");
        assertThat(response.body()).contains("\"userId\":");
        assertThat(response.body()).contains("\"createdAt\":");
        assertThat(response.body()).contains("\"updatedAt\":");
    }

    @Test
    @DisplayName("인증 없이 프로필을 조회하면 401을 반환한다.")
    void getMyProfileUnauthorized() throws IOException, InterruptedException {
        // when
        HttpResponse<String> response = sendProfileRequest(null);

        // then
        assertThat(response.statusCode()).isEqualTo(401);
    }

    private void verifyEmail(String email) {
        authService.sendVerificationCode(new EmailVerificationSendRequest(email));
        String code = emailVerificationRepository.findByEmail(email).orElseThrow().getCode();
        authService.verifyCode(new EmailVerificationVerifyRequest(email, code));
    }

    private HttpResponse<String> sendProfileRequest(String authorizationHeader) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/api/users/me"))
                .GET();

        if (authorizationHeader != null) {
            requestBuilder.header(HttpHeaders.AUTHORIZATION, authorizationHeader);
        }

        return HttpClient.newHttpClient().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
