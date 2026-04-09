package com.jinju.jinjuwiki.domain.upload.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jinju.jinjuwiki.domain.upload.dto.request.ImageUploadRequest;
import com.jinju.jinjuwiki.domain.upload.dto.response.ImageUploadResponse;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

// 업로드 서비스 단위 테스트 클래스
class UploadServiceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("이미지를 업로드하면 저장 경로를 반환한다.")
    void uploadImageSuccess() throws Exception {
        // given
        UploadServiceImpl uploadService = createUploadService();
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "sample.png",
                "image/png",
                "image-content".getBytes()
        );

        // when
        ImageUploadResponse response = uploadService.uploadImage(new ImageUploadRequest(image));

        // then
        assertThat(response.imageUrl()).startsWith("/uploads/images/");
        assertThat(Files.list(tempDir).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("빈 파일을 업로드하면 예외가 발생한다.")
    void uploadImageFailWhenFileIsEmpty() {
        // given
        UploadServiceImpl uploadService = createUploadService();
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "sample.png",
                "image/png",
                new byte[0]
        );

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> uploadService.uploadImage(new ImageUploadRequest(image))
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EMPTY_UPLOAD_FILE);
    }

    @Test
    @DisplayName("이미지 형식이 아니면 예외가 발생한다.")
    void uploadImageFailWhenFileTypeIsInvalid() {
        // given
        UploadServiceImpl uploadService = createUploadService();
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "sample.txt",
                "text/plain",
                "file-content".getBytes()
        );

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> uploadService.uploadImage(new ImageUploadRequest(image))
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_UPLOAD_FILE_TYPE);
    }

    // 테스트용 업로드 서비스 생성 함수
    private UploadServiceImpl createUploadService() {
        UploadServiceImpl uploadService = new UploadServiceImpl();
        ReflectionTestUtils.setField(uploadService, "imageUploadPath", tempDir.toString());
        ReflectionTestUtils.setField(uploadService, "imageUrlPrefix", "/uploads/images/");
        return uploadService;
    }
}
