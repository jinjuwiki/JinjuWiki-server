package com.jinju.jinjuwiki.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// 비밀번호 재설정 완료 요청 DTO
@Schema(description = "비밀번호 재설정 완료 요청")
public record PasswordResetConfirmRequest(
        @Schema(description = "비밀번호 재설정 완료에 사용할 reset token", example = "reset-token-example")
        @NotBlank(message = "재설정 토큰은 비어 있을 수 없습니다.")
        String resetToken,

        @Schema(description = "새 비밀번호", example = "newPassword1234")
        @NotBlank(message = "새 비밀번호는 비어 있을 수 없습니다.")
        @Size(min = 8, message = "새 비밀번호는 8자 이상이어야 합니다.")
        String newPassword
) {
}
