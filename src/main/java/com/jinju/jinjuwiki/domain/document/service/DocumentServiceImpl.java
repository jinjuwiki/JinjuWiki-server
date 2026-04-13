package com.jinju.jinjuwiki.domain.document.service;

import com.jinju.jinjuwiki.domain.category.entity.Category;
import com.jinju.jinjuwiki.domain.category.repository.CategoryRepository;
import com.jinju.jinjuwiki.domain.document.dto.request.DocumentCreateRequest;
import com.jinju.jinjuwiki.domain.document.dto.request.DocumentUpdateRequest;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentCreateResponse;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentDetailResponse;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import com.jinju.jinjuwiki.domain.document.mapper.DocumentCreateResponseMapper;
import com.jinju.jinjuwiki.domain.document.mapper.DocumentDetailResponseMapper;
import com.jinju.jinjuwiki.domain.document.repository.DocumentRepository;
import com.jinju.jinjuwiki.domain.document.support.DocumentContentJsonCodec;
import com.jinju.jinjuwiki.domain.search.service.DocumentViewLogService;
import com.jinju.jinjuwiki.domain.search.service.RedisDocumentViewCountBuffer;
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
    private final RedisDocumentViewCountBuffer redisDocumentViewCountBuffer;

    @Override
    @Transactional
    // 문서 생성 응답 조립 메서드
    public DocumentCreateResponse createDocument(DocumentCreateRequest request, Long currentUserId) {
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

        Document savedDocument = documentRepository.save(document);
        return DocumentCreateResponseMapper.toResponse(savedDocument);
    }

    @Override
    @Transactional
    // 문서 상세 응답 조립 메서드
    public DocumentDetailResponse getDocument(Long id, Long viewerUserId, String viewerIp) {
        Document document = documentDomainService.getDocument(id);

        if (recordDocumentView(document, viewerUserId, viewerIp)) {
            bufferDocumentViewCount(document);
        }
        return DocumentDetailResponseMapper.toResponse(document);
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
    private boolean recordDocumentView(Document document, Long viewerUserId, String viewerIp) {
        if (viewerUserId != null) {
            return recordUserDocumentView(document, viewerUserId);
        }

        return recordAnonymousDocumentView(document, viewerIp);
    }

    // 로그인 사용자 조회 처리 메서드
    private boolean recordUserDocumentView(Document document, Long viewerUserId) {
        return documentViewLogService.save(document, viewerUserId, null);
    }

    // 비로그인 사용자 조회 처리 메서드
    private boolean recordAnonymousDocumentView(Document document, String viewerIp) {
        if (viewerIp == null || viewerIp.isBlank()) {
            return false;
        }

        return documentViewLogService.save(document, null, viewerIp);
    }

    // 조회수 Redis 누적 반영 메서드
    private void bufferDocumentViewCount(Document document) {
        redisDocumentViewCountBuffer.increment(document.getId());
        document.increaseViewCount();
    }
}
