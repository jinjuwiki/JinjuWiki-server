package com.jinju.jinjuwiki.domain.document.service;

import com.jinju.jinjuwiki.domain.document.dto.request.DocumentCreateRequest;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentCreateResponse;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentDetailResponse;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentSummaryResponse;
import com.jinju.jinjuwiki.domain.document.dto.request.DocumentUpdateRequest;
import com.jinju.jinjuwiki.global.response.PageResponse;

public interface DocumentService {

    DocumentCreateResponse createDocument(DocumentCreateRequest request, Long currentUserId);

    DocumentDetailResponse getDocument(Long documentId);

    PageResponse<DocumentSummaryResponse> getDocuments(Long categoryId, int page, int size);

    PageResponse<DocumentSummaryResponse> searchDocuments(String keyword, Long categoryId, int page, int size);

    DocumentDetailResponse updateDocument(Long documentId, DocumentUpdateRequest request, Long currentUserId);

    void deleteDocument(Long documentId, Long currentUserId);
}
