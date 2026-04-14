package com.jinju.jinjuwiki.domain.auth.controller;

import com.jinju.jinjuwiki.domain.auth.dto.request.PasswordResetRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.PasswordResetConfirmRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.PasswordResetVerifyRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationSendRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.EmailVerificationSendResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationVerifyRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.EmailVerificationVerifyResponse;
import com.jinju.jinjuwiki.domain.auth.dto.response.PasswordResetVerifyResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.LoginRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.LoginResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.SignupRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.SignupResponse;
import com.jinju.jinjuwiki.domain.auth.service.AuthService;
import com.jinju.jinjuwiki.global.error.ErrorResponse;
import com.jinju.jinjuwiki.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "인증코드 발송 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 이메일 형식 등 요청 본문 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "이메일 인증 요청 횟수 초과",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "인증 메일 발송 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    // 이메일 인증코드 발송 응답 코드 명세
    // 이메일 인증코드 발송 엔드포인트
    public ResponseEntity<ApiResponse<EmailVerificationSendResponse>> sendVerificationCode(
            @Valid @RequestBody EmailVerificationSendRequest request
    ) {
        EmailVerificationSendResponse response = authService.sendVerificationCode(request);
        return ResponseEntity.ok(ApiResponse.of("이메일 인증코드를 발송했습니다.", response));
    }

    @PostMapping("/email/verify")
    @Operation(summary = "이메일 인증코드 검증", description = "발송된 이메일 인증코드를 검증합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "이메일 인증 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 인증코드 또는 만료된 인증코드",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "이메일 인증 요청을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "인증코드 검증 시도 횟수 초과",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    // 이메일 인증코드 검증 응답 코드 명세
    // 이메일 인증코드 검증 엔드포인트
    public ResponseEntity<ApiResponse<EmailVerificationVerifyResponse>> verifyCode(
            @Valid @RequestBody EmailVerificationVerifyRequest request
    ) {
        EmailVerificationVerifyResponse response = authService.verifyCode(request);
        return ResponseEntity.ok(ApiResponse.of("이메일 인증이 완료되었습니다.", response));
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "이메일 인증이 완료된 계정으로 회원가입을 진행합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 본문 또는 이메일 인증 미완료",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "이메일 인증 요청을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "중복된 이메일 또는 닉네임",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    // 회원가입 응답 코드 명세
    // 회원가입 엔드포인트
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of("회원가입이 완료되었습니다.", response));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 JWT access token을 발급합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 본문",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "이메일 또는 비밀번호 불일치",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    // 로그인 응답 코드 명세
    // 로그인 엔드포인트
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.of("로그인에 성공했습니다.", response));
    }

    @PostMapping("/password/reset/request")
    @Operation(summary = "비밀번호 재설정 요청", description = "비밀번호 재설정 메일 발송을 요청합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 재설정 요청 접수 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 이메일 형식 등 요청 본문 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "비밀번호 재설정 요청 횟수 초과",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "재설정 메일 발송 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    // 비밀번호 재설정 요청 응답 코드 명세
    // 비밀번호 재설정 요청 엔드포인트
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호 재설정 요청이 접수되었습니다."));
    }

    @PostMapping("/password/reset/verify")
    @Operation(summary = "비밀번호 재설정 인증코드 확인", description = "비밀번호 재설정 인증코드를 확인하고 reset token을 발급합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "비밀번호 재설정 인증코드 확인 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 인증코드 또는 만료된 인증코드",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "비밀번호 재설정 요청을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "429",
                    description = "인증코드 검증 시도 횟수 초과",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    // 비밀번호 재설정 인증코드 확인 응답 코드 명세
    // 비밀번호 재설정 인증코드 확인 엔드포인트
    public ResponseEntity<ApiResponse<PasswordResetVerifyResponse>> verifyPasswordResetCode(
            @Valid @RequestBody PasswordResetVerifyRequest request
    ) {
        PasswordResetVerifyResponse response = authService.verifyPasswordResetCode(request);
        return ResponseEntity.ok(ApiResponse.of("인증코드가 확인되었습니다.", response));
    }

    @PostMapping("/password/reset/confirm")
    @Operation(summary = "비밀번호 재설정 완료", description = "reset token으로 새 비밀번호를 설정합니다.")
    // 비밀번호 재설정 완료 엔드포인트
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request
    ) {
        authService.confirmPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success("비밀번호가 재설정되었습니다."));
    }
}
