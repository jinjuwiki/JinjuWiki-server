package com.jinju.jinjuwiki.domain.auth.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationVerifyRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationSendRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.PasswordResetRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.PasswordResetConfirmRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.PasswordResetVerifyRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.PasswordResetVerifyResponse;
import com.jinju.jinjuwiki.domain.auth.service.AuthService;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import com.jinju.jinjuwiki.global.error.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

// 인증 컨트롤러 MockMvc 단위 테스트 클래스
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("이메일 인증 발송 제한 초과 시 429와 전용 에러 코드를 반환한다.")
    void sendVerificationCodeRateLimited() throws Exception {
        // given
        EmailVerificationSendRequest request = new EmailVerificationSendRequest("verify@test.com");
        doThrow(new BusinessException(ErrorCode.EMAIL_VERIFICATION_SEND_RATE_LIMITED))
                .when(authService)
                .sendVerificationCode(request);

        // when & then
        mockMvc.perform(post("/api/auth/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_SEND_RATE_LIMITED"))
                .andExpect(jsonPath("$.message").value("이메일 인증 요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."))
                .andExpect(jsonPath("$.path").value("/api/auth/email/send"));

        verify(authService).sendVerificationCode(request);
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 제한 초과 시 429와 전용 에러 코드를 반환한다.")
    void requestPasswordResetRateLimited() throws Exception {
        // given
        PasswordResetRequest request = new PasswordResetRequest("reset@test.com");
        doThrow(new BusinessException(ErrorCode.PASSWORD_RESET_REQUEST_RATE_LIMITED))
                .when(authService)
                .requestPasswordReset(request);

        // when & then
        mockMvc.perform(post("/api/auth/password/reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("PASSWORD_RESET_REQUEST_RATE_LIMITED"))
                .andExpect(jsonPath("$.message").value("비밀번호 재설정 요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."))
                .andExpect(jsonPath("$.path").value("/api/auth/password/reset/request"));

        verify(authService).requestPasswordReset(request);
    }

    @Test
    @DisplayName("이메일 인증코드 검증 제한 초과 시 429와 전용 에러 코드를 반환한다.")
    void verifyCodeRateLimited() throws Exception {
        // given
        EmailVerificationVerifyRequest request = new EmailVerificationVerifyRequest("verify@test.com", "123456");
        doThrow(new BusinessException(ErrorCode.EMAIL_VERIFICATION_ATTEMPT_EXCEEDED))
                .when(authService)
                .verifyCode(request);

        // when & then
        mockMvc.perform(post("/api/auth/email/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("EMAIL_VERIFICATION_ATTEMPT_EXCEEDED"))
                .andExpect(jsonPath("$.message").value("인증코드 검증 시도가 너무 많습니다. 잠시 후 다시 시도해 주세요."))
                .andExpect(jsonPath("$.path").value("/api/auth/email/verify"));

        verify(authService).verifyCode(request);
    }

    @Test
    @DisplayName("비밀번호 재설정 인증코드 확인에 성공하면 reset token을 반환한다.")
    void verifyPasswordResetCodeSuccess() throws Exception {
        // given
        PasswordResetVerifyRequest request = new PasswordResetVerifyRequest("reset@test.com", "123456");
        PasswordResetVerifyResponse response = new PasswordResetVerifyResponse(
                "password-reset-token",
                LocalDateTime.of(2026, 4, 13, 16, 5, 0),
                LocalDateTime.of(2026, 4, 13, 16, 15, 0)
        );
        when(authService.verifyPasswordResetCode(request)).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/password/reset/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("인증코드가 확인되었습니다."))
                .andExpect(jsonPath("$.data.resetToken").value("password-reset-token"))
                .andExpect(jsonPath("$.data.verifiedAt").value("2026-04-13T16:05:00"))
                .andExpect(jsonPath("$.data.expiresAt").value("2026-04-13T16:15:00"));

        verify(authService).verifyPasswordResetCode(request);
    }

    @Test
    @DisplayName("비밀번호 재설정 완료에 성공하면 성공 메시지를 반환한다.")
    void confirmPasswordResetSuccess() throws Exception {
        // given
        PasswordResetConfirmRequest request = new PasswordResetConfirmRequest(
                "password-reset-token",
                "newPassword123!"
        );

        // when & then
        mockMvc.perform(post("/api/auth/password/reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("비밀번호가 재설정되었습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(authService).confirmPasswordReset(request);
    }
}
