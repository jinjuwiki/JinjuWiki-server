package com.jinju.jinjuwiki.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "내 닉네임 수정 요청")
public record UserNicknameUpdateRequest(
        @Schema(description = "변경할 새 닉네임", example = "jinju-editor")
        @NotBlank(message = "닉네임은 비어 있을 수 없습니다.")
        @Size(max = 30, message = "닉네임은 30자 이하로 입력해야 합니다.")
        String nickname
) {
}
