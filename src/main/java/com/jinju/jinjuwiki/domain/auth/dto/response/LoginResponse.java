package com.jinju.jinjuwiki.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
        @Schema(description = "토큰 타입", example = "Bearer")
        String tokenType,
        @Schema(description = "로그인한 사용자 ID", example = "7")
        Long userId,
        @Schema(description = "로그인한 사용자 이메일", example = "user@jinjuwiki.com")
        String email,
        @Schema(description = "로그인한 사용자 닉네임", example = "jinju-admin")
        String nickname,
        @Schema(description = "로그인한 사용자 권한", example = "USER")
        String role
) {
}
