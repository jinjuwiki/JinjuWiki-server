package com.jinju.jinjuwiki.domain.auth.service;

import com.jinju.jinjuwiki.domain.auth.dto.request.LoginRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.LoginResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationSendRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.EmailVerificationSendResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationVerifyRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.EmailVerificationVerifyResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.SignupRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.SignupResponse;

public interface AuthService {

    EmailVerificationSendResponse sendVerificationCode(EmailVerificationSendRequest request);

    EmailVerificationVerifyResponse verifyCode(EmailVerificationVerifyRequest request);

    SignupResponse signup(SignupRequest request);

    LoginResponse login(LoginRequest request);
}
