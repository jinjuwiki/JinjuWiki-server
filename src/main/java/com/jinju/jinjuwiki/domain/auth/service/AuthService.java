package com.jinju.jinjuwiki.domain.auth.service;

import com.jinju.jinjuwiki.domain.auth.dto.request.PasswordResetRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.LoginRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.LoginResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationSendRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.EmailVerificationSendResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationVerifyRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.EmailVerificationVerifyResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.SignupRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.SignupResponse;

// 인증 서비스 인터페이스
public interface AuthService {

    // 이메일 인증코드 발송 메서드
    EmailVerificationSendResponse sendVerificationCode(EmailVerificationSendRequest request);

    // 이메일 인증코드 검증 메서드
    EmailVerificationVerifyResponse verifyCode(EmailVerificationVerifyRequest request);

    // 회원가입 메서드
    SignupResponse signup(SignupRequest request);

    // 로그인 메서드
    LoginResponse login(LoginRequest request);

    // 비밀번호 재설정 요청 메서드
    void requestPasswordReset(PasswordResetRequest request);
}
