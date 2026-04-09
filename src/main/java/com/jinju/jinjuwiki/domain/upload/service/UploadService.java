package com.jinju.jinjuwiki.domain.upload.service;

import com.jinju.jinjuwiki.domain.upload.dto.request.ImageUploadRequest;
import com.jinju.jinjuwiki.domain.upload.dto.response.ImageUploadResponse;

public interface UploadService {

    ImageUploadResponse uploadImage(ImageUploadRequest request);
}
