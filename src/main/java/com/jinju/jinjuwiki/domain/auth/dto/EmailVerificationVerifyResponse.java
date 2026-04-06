package com.jinju.jinjuwiki.domain.auth.dto;

import java.time.LocalDateTime;

public record EmailVerificationVerifyResponse(
        String email,
        boolean verified,
        LocalDateTime verifiedAt
) {
}
