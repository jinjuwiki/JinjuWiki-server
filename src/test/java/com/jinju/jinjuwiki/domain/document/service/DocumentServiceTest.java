package com.jinju.jinjuwiki.domain.document.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinju.jinjuwiki.domain.category.entity.Category;
import com.jinju.jinjuwiki.domain.category.repository.CategoryRepository;
import com.jinju.jinjuwiki.domain.document.dto.request.DocumentCreateRequest;
import com.jinju.jinjuwiki.domain.document.dto.request.DocumentUpdateRequest;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentCreateResponse;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentDetailResponse;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentSummaryResponse;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import com.jinju.jinjuwiki.domain.document.repository.DocumentRepository;
import com.jinju.jinjuwiki.domain.document.support.DocumentContentJsonCodec;
import com.jinju.jinjuwiki.domain.search.service.DocumentViewLogService;
import com.jinju.jinjuwiki.domain.search.service.RedisDocumentViewCountBuffer;
import com.jinju.jinjuwiki.domain.user.entity.User;
import com.jinju.jinjuwiki.domain.user.entity.UserRole;
import com.jinju.jinjuwiki.domain.user.repository.UserRepository;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import com.jinju.jinjuwiki.global.response.PageResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

// 문서 서비스 Mockito 단위 테스트 클래스
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DocumentDomainService documentDomainService;

    @Mock
    private DocumentViewLogService documentViewLogService;

    @Mock
    private RedisDocumentViewCountBuffer redisDocumentViewCountBuffer;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Test
    @DisplayName("문서를 생성하면 새 스펙 필드가 함께 저장된다.")
    void createDocumentSuccess() {
        // given
        User author = createUser(1L, "doc1@test.com", "docUser");
        Category category = createCategory(10L, "학교");
        JsonNode contentJson = createContentJson();
        DocumentCreateRequest request = new DocumentCreateRequest(
                "문서 제목",
                "문서 요약",
                10L,
                null,
                2024,
                contentJson
        );
        Document savedDocument = Document.builder()
                .title("문서 제목")
                .content(DocumentContentJsonCodec.writeValue(contentJson))
                .summary("문서 요약")
                .eventYear(2024)
                .contentJson(DocumentContentJsonCodec.writeValue(contentJson))
                .author(author)
                .category(category)
                .build();
        ReflectionTestUtils.setField(savedDocument, "id", 100L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);

        // when
        DocumentCreateResponse response = documentService.createDocument(request, 1L);

        // then
        assertThat(response.documentId()).isEqualTo(100L);
        assertThat(response.summary()).isEqualTo("문서 요약");
        assertThat(response.eventYear()).isEqualTo(2024);
        assertThat(response.contentJson()).isEqualTo(contentJson);
        assertThat(response.authorNickname()).isEqualTo("docUser");
        verify(userRepository).findById(1L);
        verify(categoryRepository).findById(10L);
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    @DisplayName("존재하지 않는 작성자 ID로 문서를 생성하면 예외가 발생한다.")
    void createDocumentFailWhenUserNotFound() {
        // given
        DocumentCreateRequest request = new DocumentCreateRequest(
                "문서 제목",
                "문서 요약",
                10L,
                null,
                2024,
                createContentJson()
        );
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> documentService.createDocument(request, 999L)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("문서를 조회하면 조회수가 증가한다.")
    void getDocumentSuccess() {
        // given
        Document document = Document.builder()
                .title("수학 공부법")
                .content(DocumentContentJsonCodec.writeValue(createContentJson()))
                .summary("시험 대비 요약")
                .eventYear(2024)
                .contentJson(DocumentContentJsonCodec.writeValue(createContentJson()))
                .author(createUser(2L, "doc2@test.com", "docUser2"))
                .category(createCategory(20L, "학생"))
                .build();
        ReflectionTestUtils.setField(document, "id", 200L);

        when(documentDomainService.getDocument(200L)).thenReturn(document);
        when(documentViewLogService.save(document, 2L, null)).thenReturn(true);
        when(redisDocumentViewCountBuffer.increment(200L)).thenReturn(1L);

        // when
        DocumentDetailResponse response = documentService.getDocument(200L, 2L, "127.0.0.1");

        // then
        assertThat(response.documentId()).isEqualTo(200L);
        assertThat(response.viewCount()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("수학 공부법");
        verify(documentDomainService).getDocument(200L);
        verify(documentViewLogService).save(document, 2L, null);
        verify(redisDocumentViewCountBuffer).increment(200L);
    }

    @Test
    @DisplayName("조회수 버퍼 반영이 실패해도 문서 상세 조회는 성공한다.")
    void getDocumentSuccessWhenViewCountBufferFails() {
        // given
        Document document = Document.builder()
                .title("수학 공부법")
                .content(DocumentContentJsonCodec.writeValue(createContentJson()))
                .summary("시험 대비 요약")
                .eventYear(2024)
                .contentJson(DocumentContentJsonCodec.writeValue(createContentJson()))
                .author(createUser(2L, "doc2@test.com", "docUser2"))
                .category(createCategory(20L, "학생"))
                .build();
        ReflectionTestUtils.setField(document, "id", 200L);

        when(documentDomainService.getDocument(200L)).thenReturn(document);
        when(documentViewLogService.save(document, 2L, null)).thenReturn(true);
        when(redisDocumentViewCountBuffer.increment(200L)).thenThrow(new IllegalStateException("redis unavailable"));

        // when
        DocumentDetailResponse response = documentService.getDocument(200L, 2L, "127.0.0.1");

        // then
        assertThat(response.documentId()).isEqualTo(200L);
        assertThat(response.viewCount()).isEqualTo(1L);
        verify(documentDomainService).getDocument(200L);
        verify(documentViewLogService).save(document, 2L, null);
        verify(redisDocumentViewCountBuffer).increment(200L);
    }

    @Test
    @DisplayName("문서 목록은 최신순으로 페이지 조회할 수 있다.")
    void getDocumentsSuccess() {
        // given
        Document older = createDocument(
                301L,
                "첫 번째 글",
                "요약 1",
                2023,
                createUser(3L, "doc3@test.com", "docUser3"),
                createCategory(30L, "사건사고")
        );
        Document newer = createDocument(
                302L,
                "두 번째 글",
                "요약 2",
                2024,
                createUser(3L, "doc3@test.com", "docUser3"),
                createCategory(30L, "사건사고")
        );
        Page<Document> pageResponse = new PageImpl<>(List.of(newer, older));

        when(documentRepository.findByCategoryIdOrderByCreatedAtDesc(eq(30L), any(Pageable.class)))
                .thenReturn(pageResponse);

        // when
        PageResponse<DocumentSummaryResponse> response = documentService.getDocuments(30L, null, 0, 10);

        // then
        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).title()).isEqualTo("두 번째 글");
        assertThat(response.content().get(1).title()).isEqualTo("첫 번째 글");
        verify(documentRepository).findByCategoryIdOrderByCreatedAtDesc(eq(30L), any(Pageable.class));
    }

    @Test
    @DisplayName("작성자는 자신의 문서를 수정할 수 있다.")
    void updateDocumentSuccess() {
        // given
        User author = createUser(4L, "doc4@test.com", "docUser4");
        Category category = createCategory(40L, "학교");
        Category updatedCategory = createCategory(41L, "선생님");
        Document document = createDocument(400L, "원래 제목", "원래 요약", 2023, author, category);
        Document schoolDocument = createDocument(401L, "진주고등학교", "학교 요약", 2024, author, category);
        JsonNode updatedContentJson = createContentJson();
        when(documentDomainService.getDocument(400L)).thenReturn(document);
        doNothing().when(documentDomainService).validateAuthor(document, 4L);
        when(categoryRepository.findById(41L)).thenReturn(Optional.of(updatedCategory));
        when(documentRepository.findById(401L)).thenReturn(Optional.of(schoolDocument));

        // when
        DocumentDetailResponse response = documentService.updateDocument(
                400L,
                new DocumentUpdateRequest("수정 제목", "수정 요약", 41L, 401L, 2024, updatedContentJson),
                4L
        );

        // then
        assertThat(response.title()).isEqualTo("수정 제목");
        assertThat(response.summary()).isEqualTo("수정 요약");
        assertThat(response.eventYear()).isEqualTo(2024);
        assertThat(response.contentJson()).isEqualTo(updatedContentJson);
        assertThat(response.categoryName()).isEqualTo("선생님");
        assertThat(response.schoolDocumentId()).isEqualTo(401L);
        assertThat(response.schoolName()).isEqualTo("진주고등학교");
        verify(documentDomainService).getDocument(400L);
        verify(documentDomainService).validateAuthor(document, 4L);
        verify(categoryRepository).findById(41L);
        verify(documentRepository).findById(401L);
    }

    @Test
    @DisplayName("작성자가 아니면 문서를 수정할 수 없다.")
    void updateDocumentFailWhenNotAuthor() {
        // given
        Document document = createDocument(
                500L,
                "제목",
                "요약",
                2024,
                createUser(5L, "doc5@test.com", "author5"),
                createCategory(50L, "학교")
        );
        when(documentDomainService.getDocument(500L)).thenReturn(document);
        doThrow(new BusinessException(ErrorCode.FORBIDDEN_DOCUMENT_ACCESS))
                .when(documentDomainService)
                .validateAuthor(document, 6L);

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> documentService.updateDocument(
                        500L,
                        new DocumentUpdateRequest("수정 제목", "수정 요약", 50L, null, 2024, createContentJson()),
                        6L
                )
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_DOCUMENT_ACCESS);
        verify(documentDomainService).getDocument(500L);
        verify(documentDomainService).validateAuthor(document, 6L);
    }

    @Test
    @DisplayName("작성자는 자신의 문서를 삭제할 수 있다.")
    void deleteDocumentSuccess() {
        // given
        Document document = createDocument(
                700L,
                "삭제 제목",
                "삭제 요약",
                2024,
                createUser(7L, "doc7@test.com", "docUser7"),
                createCategory(70L, "기타")
        );
        when(documentDomainService.getDocument(700L)).thenReturn(document);
        doNothing().when(documentDomainService).validateAuthor(document, 7L);

        // when
        documentService.deleteDocument(700L, 7L);

        // then
        verify(documentDomainService).getDocument(700L);
        verify(documentDomainService).validateAuthor(document, 7L);
        verify(documentRepository).delete(document);
    }

    @Test
    @DisplayName("작성자가 아니면 문서를 삭제할 수 없다.")
    void deleteDocumentFailWhenNotAuthor() {
        // given
        Document document = createDocument(
                800L,
                "삭제 전 제목",
                "삭제 전 요약",
                2024,
                createUser(8L, "doc8@test.com", "author8"),
                createCategory(80L, "학교")
        );
        when(documentDomainService.getDocument(800L)).thenReturn(document);
        doThrow(new BusinessException(ErrorCode.FORBIDDEN_DOCUMENT_ACCESS))
                .when(documentDomainService)
                .validateAuthor(document, 9L);

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> documentService.deleteDocument(800L, 9L)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_DOCUMENT_ACCESS);
        verify(documentDomainService).getDocument(800L);
        verify(documentDomainService).validateAuthor(document, 9L);
    }

    @Test
    @DisplayName("키워드로 제목과 요약을 검색할 수 있다.")
    void searchDocumentsSuccess() {
        // given
        Document document = createDocument(
                1000L,
                "수학 공부법",
                "시험 대비 공부 루틴",
                2024,
                createUser(10L, "doc10@test.com", "docUser10"),
                createCategory(100L, "학생")
        );
        Page<Document> searchResult = new PageImpl<>(List.of(document));
        when(documentRepository.searchByKeyword(eq("공부"), any(Pageable.class))).thenReturn(searchResult);

        // when
        PageResponse<DocumentSummaryResponse> response = documentService.searchDocuments("공부", null, null, 0, 10);

        // then
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).title()).isEqualTo("수학 공부법");
        verify(documentRepository).searchByKeyword(eq("공부"), any(Pageable.class));
    }

    @Test
    @DisplayName("검색은 카테고리 필터와 함께 사용할 수 있다.")
    void searchDocumentsWithCategory() {
        // given
        Document document = createDocument(
                1100L,
                "수학 시험 대비",
                "공부 계획",
                2024,
                createUser(11L, "doc11@test.com", "docUser11"),
                createCategory(110L, "학생")
        );
        Page<Document> searchResult = new PageImpl<>(List.of(document));
        when(documentRepository.searchByCategoryAndKeyword(eq(110L), eq("시험"), any(Pageable.class)))
                .thenReturn(searchResult);

        // when
        PageResponse<DocumentSummaryResponse> response = documentService.searchDocuments("시험", 110L, null, 0, 10);

        // then
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).categoryName()).isEqualTo("학생");
        verify(documentRepository).searchByCategoryAndKeyword(eq(110L), eq("시험"), any(Pageable.class));
    }

    @Test
    @DisplayName("빈 검색어로 검색하면 예외가 발생한다.")
    void searchDocumentsFailWhenKeywordIsBlank() {
        // given
        String keyword = "   ";

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> documentService.searchDocuments(keyword, null, null, 0, 10)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("학교 하위 카테고리 문서는 학교 문서 ID가 필요하다.")
    void createDocumentFailWhenSchoolDocumentIdMissing() {
        User author = createUser(12L, "doc12@test.com", "docUser12");
        Category category = createCategory(120L, "학생");
        when(userRepository.findById(12L)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(120L)).thenReturn(Optional.of(category));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> documentService.createDocument(
                        new DocumentCreateRequest("학생 문서", "요약", 120L, null, 2024, createContentJson()),
                        12L
                )
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT);
    }

    private JsonNode createContentJson() {
        // 테스트용 본문 JSON 생성 함수
        JsonNode documentNode = OBJECT_MAPPER.createObjectNode();
        ((com.fasterxml.jackson.databind.node.ObjectNode) documentNode).put("type", "doc");
        ((com.fasterxml.jackson.databind.node.ObjectNode) documentNode).putArray("content");
        return documentNode;
    }

    // 테스트용 사용자 생성 함수
    private User createUser(Long id, String email, String nickname) {
        User user = User.builder()
                .email(email)
                .password("encoded-password")
                .nickname(nickname)
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    // 테스트용 카테고리 생성 함수
    private Category createCategory(Long id, String name) {
        Category category = Category.builder()
                .name(name)
                .build();
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    // 테스트용 문서 생성 함수
    private Document createDocument(Long id, String title, String summary, Integer eventYear, User author, Category category) {
        Document document = Document.builder()
                .title(title)
                .content(DocumentContentJsonCodec.writeValue(createContentJson()))
                .summary(summary)
                .eventYear(eventYear)
                .contentJson(DocumentContentJsonCodec.writeValue(createContentJson()))
                .author(author)
                .category(category)
                .schoolDocument(null)
                .build();
        ReflectionTestUtils.setField(document, "id", id);
        return document;
    }
}
