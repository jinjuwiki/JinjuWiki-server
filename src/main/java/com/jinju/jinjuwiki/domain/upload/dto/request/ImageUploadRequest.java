package com.jinju.jinjuwiki.domain.upload.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record ImageUploadRequest(
        @NotNull(message = "이미지 파일은 필수입니다.")
        MultipartFile image
) {
}
