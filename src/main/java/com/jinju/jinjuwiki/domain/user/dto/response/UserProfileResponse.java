package com.jinju.jinjuwiki.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "내 프로필 조회 응답")
public record UserProfileResponse(
        @Schema(description = "사용자 ID", example = "7")
        Long userId,
        @Schema(description = "사용자 이메일", example = "user@jinjuwiki.com")
        String email,
        @Schema(description = "사용자 닉네임", example = "jinju-admin")
        String nickname,
        @Schema(description = "사용자 권한", example = "USER")
        String role,
        @Schema(description = "계정 생성 시각", example = "2026-04-14T10:30:00")
        LocalDateTime createdAt,
        @Schema(description = "계정 마지막 수정 시각", example = "2026-04-14T11:45:00")
        LocalDateTime updatedAt
) {
}
