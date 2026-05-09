package com.jinju.jinjuwiki.domain.document.service;

import com.jinju.jinjuwiki.domain.category.entity.Category;
import com.jinju.jinjuwiki.domain.category.entity.CategoryType;
import com.jinju.jinjuwiki.domain.category.repository.CategoryRepository;
import com.jinju.jinjuwiki.domain.document.dto.request.DocumentCreateRequest;
import com.jinju.jinjuwiki.domain.document.dto.request.DocumentUpdateRequest;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentCreateResponse;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentDetailResponse;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentSummaryResponse;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import com.jinju.jinjuwiki.domain.document.mapper.DocumentCreateResponseMapper;
import com.jinju.jinjuwiki.domain.document.mapper.DocumentDetailResponseMapper;
import com.jinju.jinjuwiki.domain.document.mapper.DocumentSummaryResponseMapper;
import com.jinju.jinjuwiki.domain.document.repository.DocumentRepository;
import com.jinju.jinjuwiki.domain.document.support.DocumentContentJsonCodec;
import com.jinju.jinjuwiki.domain.search.service.DocumentViewLogService;
import com.jinju.jinjuwiki.domain.search.service.RedisDocumentViewCountBuffer;
import com.jinju.jinjuwiki.domain.user.entity.User;
import com.jinju.jinjuwiki.domain.user.repository.UserRepository;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import com.jinju.jinjuwiki.global.response.PageResponse;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private static final Set<String> SCHOOL_CHILD_CATEGORY_NAMES = Set.of(
            CategoryType.STUDENT.getDisplayName(),
            CategoryType.TEACHER.getDisplayName(),
            CategoryType.INCIDENT.getDisplayName()
    );

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
        Document schoolDocument = resolveSchoolDocument(category, request.schoolDocumentId());

        Document document = Document.builder()
                .title(request.title())
                .content(DocumentContentJsonCodec.writeValue(request.contentJson()))
                .summary(request.summary())
                .eventYear(request.eventYear())
                .contentJson(DocumentContentJsonCodec.writeValue(request.contentJson()))
                .author(author)
                .category(category)
                .schoolDocument(schoolDocument)
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
    @Transactional(readOnly = true)
    // 문서 목록 응답 조립 메서드
    public PageResponse<DocumentSummaryResponse> getDocuments(Long categoryId, Long schoolDocumentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Document> documents;

        if (categoryId != null && schoolDocumentId != null) {
            documents = documentRepository.findByCategoryIdAndSchoolDocumentIdOrderByCreatedAtDesc(
                    categoryId,
                    schoolDocumentId,
                    pageable
            );
        } else if (categoryId != null) {
            documents = documentRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
        } else if (schoolDocumentId != null) {
            documents = documentRepository.findBySchoolDocumentIdOrderByCreatedAtDesc(schoolDocumentId, pageable);
        } else {
            documents = documentRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return PageResponse.from(documents.map(DocumentSummaryResponseMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    // 문서 검색 응답 조립 메서드
    public PageResponse<DocumentSummaryResponse> searchDocuments(
            String keyword,
            Long categoryId,
            Long schoolDocumentId,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        if (normalizedKeyword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        Page<Document> documents;

        if (categoryId != null && schoolDocumentId != null) {
            documents = documentRepository.searchByCategoryAndSchoolAndKeyword(
                    categoryId,
                    schoolDocumentId,
                    normalizedKeyword,
                    pageable
            );
        } else if (categoryId != null) {
            documents = documentRepository.searchByCategoryAndKeyword(categoryId, normalizedKeyword, pageable);
        } else if (schoolDocumentId != null) {
            documents = documentRepository.searchBySchoolAndKeyword(schoolDocumentId, normalizedKeyword, pageable);
        } else {
            documents = documentRepository.searchByKeyword(normalizedKeyword, pageable);
        }

        return PageResponse.from(documents.map(DocumentSummaryResponseMapper::toResponse));
    }

    @Override
    @Transactional
    // 문서 수정 응답 조립 메서드
    public DocumentDetailResponse updateDocument(Long id, DocumentUpdateRequest request, Long currentUserId) {
        Document document = documentDomainService.getDocument(id);
        documentDomainService.validateAuthor(document, currentUserId);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
        Document schoolDocument = resolveSchoolDocument(category, request.schoolDocumentId());

        document.update(
                request.title(),
                DocumentContentJsonCodec.writeValue(request.contentJson()),
                request.summary(),
                request.eventYear(),
                DocumentContentJsonCodec.writeValue(request.contentJson()),
                category,
                schoolDocument
        );
        return DocumentDetailResponseMapper.toResponse(document);
    }

    @Override
    @Transactional
    public void deleteDocument(Long id, Long currentUserId) {
        Document document = documentDomainService.getDocument(id);
        documentDomainService.validateAuthor(document, currentUserId);

        if (isSchoolCategory(document.getCategory()) && documentRepository.existsBySchoolDocumentId(document.getId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        documentRepository.delete(document);
    }

    private Document resolveSchoolDocument(Category category, Long schoolDocumentId) {
        if (isSchoolChildCategory(category)) {
            if (schoolDocumentId == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }

            Document schoolDocument = documentRepository.findById(schoolDocumentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));

            if (!isSchoolCategory(schoolDocument.getCategory())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }

            return schoolDocument;
        }

        if (schoolDocumentId != null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        return null;
    }

    private boolean isSchoolCategory(Category category) {
        return CategoryType.SCHOOL.getDisplayName().equals(category.getName());
    }

    private boolean isSchoolChildCategory(Category category) {
        return SCHOOL_CHILD_CATEGORY_NAMES.contains(category.getName());
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
