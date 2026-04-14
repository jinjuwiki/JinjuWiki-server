package com.jinju.jinjuwiki.domain.upload.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이미지 업로드 응답")
public record ImageUploadResponse(
        @Schema(description = "업로드된 이미지 접근 URL", example = "https://cdn.jinjuwiki.com/uploads/images/example.png")
        String imageUrl
) {
}
