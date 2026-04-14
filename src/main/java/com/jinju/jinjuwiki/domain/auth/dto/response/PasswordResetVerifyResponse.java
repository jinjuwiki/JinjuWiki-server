package com.jinju.jinjuwiki.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

// 비밀번호 재설정 인증코드 확인 응답 DTO
@Schema(description = "비밀번호 재설정 인증코드 확인 응답")
public record PasswordResetVerifyResponse(
        @Schema(description = "비밀번호 재설정 완료 요청에 사용할 reset token", example = "reset-token-example")
        String resetToken,
        @Schema(description = "인증코드 확인 시각", example = "2026-04-14T10:30:00")
        LocalDateTime verifiedAt,
        @Schema(description = "reset token 만료 시각", example = "2026-04-14T11:00:00")
        LocalDateTime expiresAt
) {
}
