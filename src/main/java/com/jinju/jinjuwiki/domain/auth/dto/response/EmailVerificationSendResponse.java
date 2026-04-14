package com.jinju.jinjuwiki.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "이메일 인증코드 발송 응답")
public record EmailVerificationSendResponse(
        @Schema(description = "인증코드가 발송된 이메일", example = "user@jinjuwiki.com")
        String email,
        @Schema(description = "인증코드 만료 시각", example = "2026-04-14T10:35:00")
        LocalDateTime expiresAt
) {
}
