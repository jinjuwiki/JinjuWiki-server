package com.jinju.jinjuwiki.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 수정 응답")
public record UserNicknameUpdateResponse(
        @Schema(description = "사용자 ID", example = "7")
        Long userId,
        @Schema(description = "변경된 닉네임", example = "jinju-editor")
        String nickname
) {
}
