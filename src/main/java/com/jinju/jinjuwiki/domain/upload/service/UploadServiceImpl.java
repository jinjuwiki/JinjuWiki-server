package com.jinju.jinjuwiki.domain.upload.service;

import com.jinju.jinjuwiki.domain.upload.dto.request.ImageUploadRequest;
import com.jinju.jinjuwiki.domain.upload.dto.response.ImageUploadResponse;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

// 이미지 저장 서비스
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png",
            "image/jpeg",
            "image/webp",
            "image/gif"
    );

    private static final Path UPLOAD_ROOT_PATH = Path.of("uploads", "images");
    private static final String UPLOAD_URL_PREFIX = "/uploads/images/";

    @Override
    public ImageUploadResponse uploadImage(ImageUploadRequest request) {
        MultipartFile image = request.image();

        validateImage(image);

        String storedFileName = createStoredFileName(image.getOriginalFilename());
        Path targetPath = UPLOAD_ROOT_PATH.resolve(storedFileName);

        try {
            Files.createDirectories(UPLOAD_ROOT_PATH);
            Files.copy(image.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return new ImageUploadResponse(UPLOAD_URL_PREFIX + storedFileName);
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        if (!ALLOWED_CONTENT_TYPES.contains(image.getContentType())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    private String createStoredFileName(String originalFilename) {
        String extension = extractExtension(originalFilename);
        return UUID.randomUUID() + extension;
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}
