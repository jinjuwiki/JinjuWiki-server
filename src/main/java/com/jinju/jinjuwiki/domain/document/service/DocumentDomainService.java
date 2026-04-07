package com.jinju.jinjuwiki.domain.document.service;

import com.jinju.jinjuwiki.domain.document.entity.Document;
import com.jinju.jinjuwiki.domain.document.repository.DocumentRepository;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentDomainService {

    private final DocumentRepository documentRepository;

    public Document getDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    public void validateAuthor(Document document, Long authorId) {
        if (!document.isWrittenBy(authorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_DOCUMENT_ACCESS);
        }
    }
}
