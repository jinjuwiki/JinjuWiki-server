package com.jinju.jinjuwiki.domain.user.dto.response;

public record UserNicknameUpdateResponse(
        Long userId,
        String nickname
) {
}
