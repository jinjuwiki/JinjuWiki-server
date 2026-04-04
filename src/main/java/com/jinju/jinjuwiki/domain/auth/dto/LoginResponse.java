package com.jinju.jinjuwiki.domain.auth.dto;

public record LoginResponse(
        Long userId,
        String email,
        String nickname,
        String role
) {
}
