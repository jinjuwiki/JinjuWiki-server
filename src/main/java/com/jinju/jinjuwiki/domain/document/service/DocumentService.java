package com.jinju.jinjuwiki.domain.document.service;

import com.jinju.jinjuwiki.domain.document.dto.request.DocumentCreateRequest;
import com.jinju.jinjuwiki.domain.document.dto.request.DocumentUpdateRequest;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import org.springframework.data.domain.Page;

public interface DocumentService {

    Document createDocument(DocumentCreateRequest request, Long currentUserId);

    Document getDocument(Long id);

    Page<Document> getDocuments(Long categoryId, int page, int size);

    Page<Document> searchDocuments(String keyword, Long categoryId, int page, int size);

    Document updateDocument(Long id, DocumentUpdateRequest request, Long currentUserId);

    void deleteDocument(Long id, Long currentUserId);
}
