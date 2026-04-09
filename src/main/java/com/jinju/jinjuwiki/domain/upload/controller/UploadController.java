package com.jinju.jinjuwiki.domain.upload.controller;

import com.jinju.jinjuwiki.domain.upload.dto.request.ImageUploadRequest;
import com.jinju.jinjuwiki.domain.upload.dto.response.ImageUploadResponse;
import com.jinju.jinjuwiki.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 이미지 업로드 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/uploads")
@Tag(name = "Upload", description = "이미지 업로드 API")
public class UploadController {

    @PostMapping("/images")
    @Operation(
            summary = "문서 본문 이미지 업로드",
            description = "문서 본문에 사용할 이미지를 업로드합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImage(
            @Valid @ModelAttribute ImageUploadRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(ApiResponse.of("이미지 업로드 구현 예정입니다.", new ImageUploadResponse(null)));
    }
}
