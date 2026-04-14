package com.jinju.jinjuwiki.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "이메일 인증코드 검증 요청")
public record EmailVerificationVerifyRequest(
        @Schema(description = "인증을 진행할 이메일", example = "user@jinjuwiki.com")
        @NotBlank(message = "이메일은 비어 있을 수 없습니다.")
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        String email,

        @Schema(description = "이메일로 전달된 6자리 인증코드", example = "123456")
        @NotBlank(message = "인증코드는 비어 있을 수 없습니다.")
        @Pattern(regexp = "\\d{6}", message = "인증코드는 6자리 숫자여야 합니다.")
        String code
) {
}
