package com.jinju.jinjuwiki.domain.auth.dto;

public record SignupResponse(
        Long userId,
        String email,
        String nickname
) {
}
