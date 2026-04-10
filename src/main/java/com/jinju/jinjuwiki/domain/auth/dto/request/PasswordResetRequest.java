package com.jinju.jinjuwiki.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 비밀번호 재설정 요청 DTO
public record PasswordResetRequest(
        @NotBlank(message = "이메일은 비어 있을 수 없습니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email
) {
}
