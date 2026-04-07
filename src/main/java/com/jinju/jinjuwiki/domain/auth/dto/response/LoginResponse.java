package com.jinju.jinjuwiki.domain.auth.dto.response;

public record LoginResponse(
        String accessToken,
        String tokenType,
        Long userId,
        String email,
        String nickname,
        String role
) {
}
