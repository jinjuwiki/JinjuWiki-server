package com.jinju.jinjuwiki.domain.auth.service;

import com.jinju.jinjuwiki.domain.auth.dto.LoginRequest;
import com.jinju.jinjuwiki.domain.auth.dto.LoginResponse;
import com.jinju.jinjuwiki.domain.auth.dto.EmailVerificationSendRequest;
import com.jinju.jinjuwiki.domain.auth.dto.EmailVerificationSendResponse;
import com.jinju.jinjuwiki.domain.auth.dto.EmailVerificationVerifyRequest;
import com.jinju.jinjuwiki.domain.auth.dto.EmailVerificationVerifyResponse;
import com.jinju.jinjuwiki.domain.auth.dto.SignupRequest;
import com.jinju.jinjuwiki.domain.auth.dto.SignupResponse;

public interface AuthService {

    EmailVerificationSendResponse sendVerificationCode(EmailVerificationSendRequest request);

    EmailVerificationVerifyResponse verifyCode(EmailVerificationVerifyRequest request);

    SignupResponse signup(SignupRequest request);

    LoginResponse login(LoginRequest request);
}
