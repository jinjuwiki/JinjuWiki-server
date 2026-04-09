package com.jinju.jinjuwiki.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserNicknameUpdateRequest(
        @NotBlank(message = "닉네임은 비어 있을 수 없습니다.")
        @Size(max = 30, message = "닉네임은 30자 이하로 입력해야 합니다.")
        String nickname
) {
}
