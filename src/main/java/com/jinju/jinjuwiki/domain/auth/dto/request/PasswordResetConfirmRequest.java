package com.jinju.jinjuwiki.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 비밀번호 재설정 완료 요청 DTO
public record PasswordResetConfirmRequest(
        @NotBlank(message = "재설정 토큰은 비어 있을 수 없습니다.")
        String resetToken,

        @NotBlank(message = "새 비밀번호는 비어 있을 수 없습니다.")
        @Size(min = 8, message = "새 비밀번호는 8자 이상이어야 합니다.")
        String newPassword
) {
}
