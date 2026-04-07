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
    private final DocumentDomainService documentDomainService;
    private final DocumentMapper documentMapper;

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
        return documentMapper.toCreateResponse(savedDocument);
    }

    @Override
    @Transactional
    public DocumentDetailResponse getDocument(Long id) {
        Document document = documentDomainService.getDocument(id);

        document.increaseViewCount();
        return documentMapper.toDetailResponse(document);
    }

    @Override
    public PageResponse<DocumentSummaryResponse> getDocuments(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Document> documents = categoryId == null
                ? documentRepository.findAllByOrderByCreatedAtDesc(pageable)
                : documentRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);

        return PageResponse.from(documents.map(documentMapper::toSummaryResponse));
    }

    @Override
    public PageResponse<DocumentSummaryResponse> searchDocuments(String keyword, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        if (normalizedKeyword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        Page<Document> documents = categoryId == null
                ? documentRepository.searchByKeyword(normalizedKeyword, pageable)
                : documentRepository.searchByCategoryAndKeyword(categoryId, normalizedKeyword, pageable);

        return PageResponse.from(documents.map(documentMapper::toSummaryResponse));
    }

    @Override
    @Transactional
    public DocumentDetailResponse updateDocument(Long id, DocumentUpdateRequest request, Long currentUserId) {
        Document document = documentDomainService.getDocument(id);
        documentDomainService.validateAuthor(document, currentUserId);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        document.update(request.title(), request.content(), category);
        return documentMapper.toDetailResponse(document);
    }

    @Override
    @Transactional
    public void deleteDocument(Long id, Long currentUserId) {
        Document document = documentDomainService.getDocument(id);
        documentDomainService.validateAuthor(document, currentUserId);
        documentRepository.delete(document);
    }
}
