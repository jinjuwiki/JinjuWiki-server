package com.jinju.jinjuwiki.domain.user.dto.response;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long userId,
        String email,
        String nickname,
        String role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
