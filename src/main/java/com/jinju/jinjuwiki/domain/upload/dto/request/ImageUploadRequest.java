package com.jinju.jinjuwiki.domain.upload.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "이미지 업로드 요청")
public record ImageUploadRequest(
        @Schema(description = "업로드할 이미지 파일", type = "string", format = "binary")
        @NotNull(message = "이미지 파일은 필수입니다.")
        MultipartFile image
) {
}
