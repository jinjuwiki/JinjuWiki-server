package com.jinju.jinjuwiki.domain.document.service;

import com.jinju.jinjuwiki.domain.category.entity.Category;
import com.jinju.jinjuwiki.domain.category.repository.CategoryRepository;
import com.jinju.jinjuwiki.domain.document.dto.request.DocumentCreateRequest;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentCreateResponse;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentDetailResponse;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentSummaryResponse;
import com.jinju.jinjuwiki.domain.document.dto.request.DocumentUpdateRequest;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import com.jinju.jinjuwiki.domain.document.repository.DocumentRepository;
import com.jinju.jinjuwiki.domain.user.entity.User;
import com.jinju.jinjuwiki.domain.user.repository.UserRepository;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import com.jinju.jinjuwiki.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public DocumentCreateResponse createDocument(DocumentCreateRequest request, Long currentUserId) {
        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        Document document = Document.builder()
                .title(request.title())
                .content(request.content())
                .author(author)
                .category(category)
                .build();

        Document savedDocument = documentRepository.save(document);
        return toCreateResponse(savedDocument);
    }

    @Override
    @Transactional
    public DocumentDetailResponse getDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));

        document.increaseViewCount();
        return toDetailResponse(document);
    }

    @Override
    public PageResponse<DocumentSummaryResponse> getDocuments(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Document> documents = categoryId == null
                ? documentRepository.findAllByOrderByCreatedAtDesc(pageable)
                : documentRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);

        return PageResponse.from(documents.map(this::toSummaryResponse));
    }

    @Override
    public PageResponse<DocumentSummaryResponse> searchDocuments(String keyword, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        if (normalizedKeyword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        Page<Document> documents = categoryId == null
                ? documentRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByCreatedAtDesc(
                        normalizedKeyword,
                        normalizedKeyword,
                        pageable
                )
                : documentRepository.findByCategoryIdAndTitleContainingIgnoreCaseOrCategoryIdAndContentContainingIgnoreCaseOrderByCreatedAtDesc(
                        categoryId,
                        normalizedKeyword,
                        categoryId,
                        normalizedKeyword,
                        pageable
                );

        return PageResponse.from(documents.map(this::toSummaryResponse));
    }

    @Override
    @Transactional
    public DocumentDetailResponse updateDocument(Long documentId, DocumentUpdateRequest request, Long currentUserId) {
        Document document = getDocumentEntity(documentId);
        validateDocumentAuthor(document, currentUserId);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        document.update(request.title(), request.content(), category);
        return toDetailResponse(document);
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId, Long currentUserId) {
        Document document = getDocumentEntity(documentId);
        validateDocumentAuthor(document, currentUserId);
        documentRepository.delete(document);
    }

    private Document getDocumentEntity(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));
    }

    private void validateDocumentAuthor(Document document, Long authorId) {
        if (!document.isWrittenBy(authorId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_DOCUMENT_ACCESS);
        }
    }

    private DocumentCreateResponse toCreateResponse(Document document) {
        return new DocumentCreateResponse(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getCategory().getId(),
                document.getCategory().getName(),
                document.getAuthor().getId(),
                document.getAuthor().getNickname(),
                document.getCreatedAt()
        );
    }

    private DocumentDetailResponse toDetailResponse(Document document) {
        return new DocumentDetailResponse(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getCategory().getId(),
                document.getCategory().getName(),
                document.getAuthor().getId(),
                document.getAuthor().getNickname(),
                document.getViewCount(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    private DocumentSummaryResponse toSummaryResponse(Document document) {
        return new DocumentSummaryResponse(
                document.getId(),
                document.getTitle(),
                document.getCategory().getId(),
                document.getCategory().getName(),
                document.getAuthor().getNickname(),
                document.getViewCount(),
                document.getCreatedAt()
        );
    }
}
