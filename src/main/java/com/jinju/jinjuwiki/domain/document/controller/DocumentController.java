package com.jinju.jinjuwiki.domain.document.controller;

import com.jinju.jinjuwiki.domain.document.dto.DocumentCreateRequest;
import com.jinju.jinjuwiki.domain.document.dto.DocumentCreateResponse;
import com.jinju.jinjuwiki.domain.document.dto.DocumentDetailResponse;
import com.jinju.jinjuwiki.domain.document.dto.DocumentSummaryResponse;
import com.jinju.jinjuwiki.domain.document.service.DocumentService;
import com.jinju.jinjuwiki.global.response.ApiResponse;
import com.jinju.jinjuwiki.global.response.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 문서 CRUD 처리 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentCreateResponse>> createDocument(
            @Valid @RequestBody DocumentCreateRequest request
    ) {
        DocumentCreateResponse response = documentService.createDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(response));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<ApiResponse<DocumentDetailResponse>> getDocument(@PathVariable Long documentId) {
        return ResponseEntity.ok(ApiResponse.of(documentService.getDocument(documentId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DocumentSummaryResponse>>> getDocuments(
            @RequestParam(required = false) Long categoryId, // 카테고리 필터
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.of(documentService.getDocuments(categoryId, page, size)));
    }
}
