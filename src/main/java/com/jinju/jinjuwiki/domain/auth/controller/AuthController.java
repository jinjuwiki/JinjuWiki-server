package com.jinju.jinjuwiki.domain.auth.controller;

import com.jinju.jinjuwiki.domain.auth.dto.request.PasswordResetRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationSendRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.EmailVerificationSendResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationVerifyRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.EmailVerificationVerifyResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.LoginRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.LoginResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.SignupRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.SignupResponse;
import com.jinju.jinjuwiki.domain.auth.service.AuthService;
import com.jinju.jinjuwiki.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 로그인, 회원가입, 재설정 요청 처리 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "회원가입 및 로그인 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/email/send")
    @Operation(summary = "이메일 인증코드 발송", description = "회원가입 전 이메일 인증코드를 발송합니다.")
    // 이메일 인증코드 발송 엔드포인트
    public ResponseEntity<ApiResponse<EmailVerificationSendResponse>> sendVerificationCode(
            @Valid @RequestBody EmailVerificationSendRequest request
    ) {
        EmailVerificationSendResponse response = authService.sendVerificationCode(request);
        return ResponseEntity.ok(ApiResponse.of("이메일 인증코드를 발송했습니다.", response));
    }

    @PostMapping("/email/verify")
    @Operation(summary = "이메일 인증코드 검증", description = "발송된 이메일 인증코드를 검증합니다.")
    // 이메일 인증코드 검증 엔드포인트
    public ResponseEntity<ApiResponse<EmailVerificationVerifyResponse>> verifyCode(
            @Valid @RequestBody EmailVerificationVerifyRequest request
    ) {
        EmailVerificationVerifyResponse response = authService.verifyCode(request);
        return ResponseEntity.ok(ApiResponse.of("이메일 인증이 완료되었습니다.", response));
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "이메일 인증이 완료된 계정으로 회원가입을 진행합니다.")
    // 회원가입 엔드포인트
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 JWT access token을 발급합니다.")
    // 로그인 엔드포인트
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.of("로그인에 성공했습니다.", response));
    }

    @PostMapping("/password/reset/request")
    @Operation(summary = "비밀번호 재설정 요청", description = "비밀번호 재설정 메일 발송을 요청합니다.")
    // 비밀번호 재설정 요청 엔드포인트
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호 재설정 요청이 접수되었습니다."));
    }
}
