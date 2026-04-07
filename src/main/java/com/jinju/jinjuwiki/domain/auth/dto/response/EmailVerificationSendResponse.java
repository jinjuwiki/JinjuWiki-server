package com.jinju.jinjuwiki.domain.auth.dto.response;

import java.time.LocalDateTime;

public record EmailVerificationSendResponse(
        String email,
        LocalDateTime expiresAt
) {
}
