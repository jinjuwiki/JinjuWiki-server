package com.jinju.jinjuwiki.domain.auth.service;

import com.jinju.jinjuwiki.domain.auth.dto.LoginRequest;
import com.jinju.jinjuwiki.domain.auth.dto.LoginResponse;
import com.jinju.jinjuwiki.domain.auth.dto.SignupRequest;
import com.jinju.jinjuwiki.domain.auth.dto.SignupResponse;

public interface AuthService {

    SignupResponse signup(SignupRequest request);

    LoginResponse login(LoginRequest request);
}
