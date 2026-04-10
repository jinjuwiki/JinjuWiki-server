package com.jinju.jinjuwiki.domain.document.service;

import com.jinju.jinjuwiki.domain.category.entity.Category;
import com.jinju.jinjuwiki.domain.category.repository.CategoryRepository;
import com.jinju.jinjuwiki.domain.document.dto.request.DocumentCreateRequest;
import com.jinju.jinjuwiki.domain.document.dto.request.DocumentUpdateRequest;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import com.jinju.jinjuwiki.domain.document.repository.DocumentRepository;
import com.jinju.jinjuwiki.domain.document.support.DocumentContentJsonCodec;
import com.jinju.jinjuwiki.domain.search.service.DocumentViewLogService;
import com.jinju.jinjuwiki.domain.user.entity.User;
import com.jinju.jinjuwiki.domain.user.repository.UserRepository;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final DocumentDomainService documentDomainService;
    private final DocumentViewLogService documentViewLogService;

    @Override
    @Transactional
    public Document createDocument(DocumentCreateRequest request, Long currentUserId) {
        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        Document document = Document.builder()
                .title(request.title())
                .content(DocumentContentJsonCodec.writeValue(request.contentJson()))
                .summary(request.summary())
                .eventYear(request.eventYear())
                .contentJson(DocumentContentJsonCodec.writeValue(request.contentJson()))
                .author(author)
                .category(category)
                .build();

        return documentRepository.save(document);
    }

    @Override
    @Transactional
    public Document getDocument(Long id, Long viewerUserId, String viewerIp) {
        Document document = documentDomainService.getDocument(id);

        document.increaseViewCount();
        recordDocumentView(document, viewerUserId, viewerIp);
        return document;
    }

    @Override
    public Page<Document> getDocuments(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return categoryId == null
                ? documentRepository.findAllByOrderByCreatedAtDesc(pageable)
                : documentRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
    }

    @Override
    public Page<Document> searchDocuments(String keyword, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        if (normalizedKeyword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        return categoryId == null
                ? documentRepository.searchByKeyword(normalizedKeyword, pageable)
                : documentRepository.searchByCategoryAndKeyword(categoryId, normalizedKeyword, pageable);
    }

    @Override
    @Transactional
    public Document updateDocument(Long id, DocumentUpdateRequest request, Long currentUserId) {
        Document document = documentDomainService.getDocument(id);
        documentDomainService.validateAuthor(document, currentUserId);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        document.update(
                request.title(),
                DocumentContentJsonCodec.writeValue(request.contentJson()),
                request.summary(),
                request.eventYear(),
                DocumentContentJsonCodec.writeValue(request.contentJson()),
                category
        );
        return document;
    }

    @Override
    @Transactional
    public void deleteDocument(Long id, Long currentUserId) {
        Document document = documentDomainService.getDocument(id);
        documentDomainService.validateAuthor(document, currentUserId);
        documentRepository.delete(document);
    }

    // 문서 조회 사용자 식별 분기 메서드
    private void recordDocumentView(Document document, Long viewerUserId, String viewerIp) {
        if (viewerUserId != null) {
            recordUserDocumentView(document, viewerUserId);
            return;
        }

        recordAnonymousDocumentView(document, viewerIp);
    }

    // 로그인 사용자 조회 처리 메서드
    private void recordUserDocumentView(Document document, Long viewerUserId) {
        documentViewLogService.save(document, viewerUserId, null);
    }

    // 비로그인 사용자 조회 처리 메서드
    private void recordAnonymousDocumentView(Document document, String viewerIp) {
        if (viewerIp == null || viewerIp.isBlank()) {
            return;
        }

        documentViewLogService.save(document, null, viewerIp);
    }
}
