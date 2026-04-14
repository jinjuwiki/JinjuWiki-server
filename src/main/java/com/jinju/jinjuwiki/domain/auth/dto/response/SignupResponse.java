package com.jinju.jinjuwiki.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 응답")
public record SignupResponse(
        @Schema(description = "생성된 사용자 ID", example = "7")
        Long userId,
        @Schema(description = "가입한 이메일", example = "user@jinjuwiki.com")
        String email,
        @Schema(description = "가입한 닉네임", example = "jinju-admin")
        String nickname
) {
}
