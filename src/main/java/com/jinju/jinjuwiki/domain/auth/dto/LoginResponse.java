package com.jinju.jinjuwiki.domain.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String email,
        String nickname,
        String role
) {
}
