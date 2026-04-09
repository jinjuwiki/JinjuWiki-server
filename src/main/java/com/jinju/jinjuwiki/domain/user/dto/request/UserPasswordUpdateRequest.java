package com.jinju.jinjuwiki.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPasswordUpdateRequest(
        @NotBlank(message = "현재 비밀번호는 비어 있을 수 없습니다.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호는 비어 있을 수 없습니다.")
        @Size(min = 8, message = "새 비밀번호는 8자 이상이어야 합니다.")
        String newPassword
) {
}
