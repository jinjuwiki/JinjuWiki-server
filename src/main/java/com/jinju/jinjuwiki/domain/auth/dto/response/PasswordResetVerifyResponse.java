package com.jinju.jinjuwiki.domain.auth.dto.response;

import java.time.LocalDateTime;

// 비밀번호 재설정 인증코드 확인 응답 DTO
public record PasswordResetVerifyResponse(
        String resetToken,
        LocalDateTime verifiedAt,
        LocalDateTime expiresAt
) {
}
