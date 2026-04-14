package com.jinju.jinjuwiki.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청")
public record LoginRequest(
        @Schema(description = "로그인 이메일", example = "user@jinjuwiki.com")
        @NotBlank(message = "이메일은 비어 있을 수 없습니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        String email,

        @Schema(description = "로그인 비밀번호", example = "password1234")
        @NotBlank(message = "비밀번호는 비어 있을 수 없습니다.")
        String password
) {
}
