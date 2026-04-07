package com.jinju.jinjuwiki.domain.auth.dto.response;

public record SignupResponse(
        Long userId,
        String email,
        String nickname
) {
}
