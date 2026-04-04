package com.jinju.jinjuwiki.domain.document.service;

import com.jinju.jinjuwiki.domain.document.dto.DocumentCreateRequest;
import com.jinju.jinjuwiki.domain.document.dto.DocumentCreateResponse;
import com.jinju.jinjuwiki.domain.document.dto.DocumentDetailResponse;
import com.jinju.jinjuwiki.domain.document.dto.DocumentSummaryResponse;
import com.jinju.jinjuwiki.domain.document.dto.DocumentUpdateRequest;
import com.jinju.jinjuwiki.global.response.PageResponse;

public interface DocumentService {

    DocumentCreateResponse createDocument(DocumentCreateRequest request);

    DocumentDetailResponse getDocument(Long documentId);

    PageResponse<DocumentSummaryResponse> getDocuments(Long categoryId, int page, int size);

    DocumentDetailResponse updateDocument(Long documentId, DocumentUpdateRequest request);

    void deleteDocument(Long documentId, Long authorId);
}
