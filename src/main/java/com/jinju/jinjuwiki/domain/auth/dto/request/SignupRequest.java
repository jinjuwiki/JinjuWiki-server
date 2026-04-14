package com.jinju.jinjuwiki.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record SignupRequest(
        @Schema(description = "회원가입 이메일", example = "user@jinjuwiki.com")
        @NotBlank(message = "이메일은 비어 있을 수 없습니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        String email,

        @Schema(description = "회원가입 비밀번호", example = "password1234")
        @NotBlank(message = "비밀번호는 비어 있을 수 없습니다.")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
        String password,

        @Schema(description = "회원가입 닉네임", example = "jinju-admin")
        @NotBlank(message = "닉네임은 비어 있을 수 없습니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하로 입력해야 합니다.")
        String nickname
) {
}
