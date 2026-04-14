package com.jinju.jinjuwiki.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "내 비밀번호 수정 요청")
public record UserPasswordUpdateRequest(
        @Schema(description = "현재 비밀번호", example = "currentPassword1234")
        @NotBlank(message = "현재 비밀번호는 비어 있을 수 없습니다.")
        String currentPassword,

        @Schema(description = "변경할 새 비밀번호", example = "newPassword1234")
        @NotBlank(message = "새 비밀번호는 비어 있을 수 없습니다.")
        @Size(min = 8, message = "새 비밀번호는 8자 이상이어야 합니다.")
        String newPassword
) {
}
