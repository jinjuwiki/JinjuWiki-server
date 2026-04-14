package com.jinju.jinjuwiki.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이메일 인증코드 발송 요청")
public record EmailVerificationSendRequest(
        @Schema(description = "인증코드를 발송할 이메일", example = "user@jinjuwiki.com")
        @NotBlank(message = "이메일은 비어 있을 수 없습니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        String email
) {
}
