package com.jinju.jinjuwiki.domain.document.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jinju.jinjuwiki.domain.auth.dto.SignupRequest;
import com.jinju.jinjuwiki.domain.auth.dto.SignupResponse;
import com.jinju.jinjuwiki.domain.auth.service.AuthService;
import com.jinju.jinjuwiki.domain.category.entity.Category;
import com.jinju.jinjuwiki.domain.category.repository.CategoryRepository;
import com.jinju.jinjuwiki.domain.document.dto.DocumentCreateRequest;
import com.jinju.jinjuwiki.domain.document.dto.DocumentCreateResponse;
import com.jinju.jinjuwiki.domain.document.dto.DocumentDetailResponse;
import com.jinju.jinjuwiki.domain.document.dto.DocumentSummaryResponse;
import com.jinju.jinjuwiki.domain.document.dto.DocumentUpdateRequest;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import com.jinju.jinjuwiki.global.response.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.jinju.jinjuwiki.domain.document.repository.DocumentRepository;

@Transactional
@SpringBootTest
class DocumentServiceTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private AuthService authService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    @DisplayName("문서를 생성하면 작성자와 카테고리 정보가 함께 반환된다.")
    void createDocumentSuccess() {
        SignupResponse user = authService.signup(new SignupRequest("doc1@test.com", "password123", "docUser"));
        Category category = categoryRepository.findByName("학교").orElseThrow();

        DocumentCreateResponse response = documentService.createDocument(
                new DocumentCreateRequest("문서 제목", "문서 본문", category.getId()),
                user.userId()
        );

        assertThat(response.documentId()).isNotNull();
        assertThat(response.authorNickname()).isEqualTo("docUser");
        assertThat(response.categoryName()).isEqualTo("학교");
    }

    @Test
    @DisplayName("존재하지 않는 작성자 ID로 문서를 생성하면 예외가 발생한다.")
    void createDocumentFailWhenUserNotFound() {
        Category category = categoryRepository.findByName("학교").orElseThrow();

        assertThatThrownBy(() -> documentService.createDocument(
                new DocumentCreateRequest("문서 제목", "문서 본문", category.getId()),
                999L
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("문서를 조회하면 조회수가 증가한다.")
    void getDocumentSuccess() {
        SignupResponse user = authService.signup(new SignupRequest("doc2@test.com", "password123", "docUser2"));
        Category category = categoryRepository.findByName("학생").orElseThrow();
        DocumentCreateResponse created = documentService.createDocument(
                new DocumentCreateRequest("수학 공부법", "개념부터 반복", category.getId()),
                user.userId()
        );

        DocumentDetailResponse response = documentService.getDocument(created.documentId());

        assertThat(response.documentId()).isEqualTo(created.documentId());
        assertThat(response.viewCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("문서 목록은 최신순으로 페이지 조회할 수 있다.")
    void getDocumentsSuccess() {
        SignupResponse user = authService.signup(new SignupRequest("doc3@test.com", "password123", "docUser3"));
        Category category = categoryRepository.findByName("사건사고").orElseThrow();

        documentService.createDocument(new DocumentCreateRequest("첫 번째 글", "내용 1", category.getId()), user.userId());
        documentService.createDocument(new DocumentCreateRequest("두 번째 글", "내용 2", category.getId()), user.userId());

        PageResponse<DocumentSummaryResponse> response = documentService.getDocuments(category.getId(), 0, 10);

        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).title()).isEqualTo("두 번째 글");
    }

    @Test
    @DisplayName("작성자는 자신의 문서를 수정할 수 있다.")
    void updateDocumentSuccess() {
        SignupResponse user = authService.signup(new SignupRequest("doc4@test.com", "password123", "docUser4"));
        Category category = categoryRepository.findByName("학교").orElseThrow();
        Category updatedCategory = categoryRepository.findByName("선생님").orElseThrow();
        DocumentCreateResponse created = documentService.createDocument(
                new DocumentCreateRequest("원래 제목", "원래 본문", category.getId()),
                user.userId()
        );

        DocumentDetailResponse response = documentService.updateDocument(
                created.documentId(),
                new DocumentUpdateRequest("수정 제목", "수정 본문", updatedCategory.getId()),
                user.userId()
        );

        assertThat(response.title()).isEqualTo("수정 제목");
        assertThat(response.content()).isEqualTo("수정 본문");
        assertThat(response.categoryName()).isEqualTo("선생님");
    }

    @Test
    @DisplayName("작성자가 아니면 문서를 수정할 수 없다.")
    void updateDocumentFailWhenNotAuthor() {
        SignupResponse author = authService.signup(new SignupRequest("doc5@test.com", "password123", "author5"));
        SignupResponse otherUser = authService.signup(new SignupRequest("doc6@test.com", "password123", "other6"));
        Category category = categoryRepository.findByName("학교").orElseThrow();
        DocumentCreateResponse created = documentService.createDocument(
                new DocumentCreateRequest("제목", "본문", category.getId()),
                author.userId()
        );

        assertThatThrownBy(() -> documentService.updateDocument(
                created.documentId(),
                new DocumentUpdateRequest("수정 제목", "수정 본문", category.getId()),
                otherUser.userId()
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN_DOCUMENT_ACCESS);
    }

    @Test
    @DisplayName("작성자는 자신의 문서를 삭제할 수 있다.")
    void deleteDocumentSuccess() {
        SignupResponse user = authService.signup(new SignupRequest("doc7@test.com", "password123", "docUser7"));
        Category category = categoryRepository.findByName("기타").orElseThrow();
        DocumentCreateResponse created = documentService.createDocument(
                new DocumentCreateRequest("삭제 제목", "삭제 본문", category.getId()),
                user.userId()
        );

        documentService.deleteDocument(created.documentId(), user.userId());

        assertThat(documentRepository.findById(created.documentId())).isEmpty();
    }

    @Test
    @DisplayName("작성자가 아니면 문서를 삭제할 수 없다.")
    void deleteDocumentFailWhenNotAuthor() {
        SignupResponse author = authService.signup(new SignupRequest("doc8@test.com", "password123", "author8"));
        SignupResponse otherUser = authService.signup(new SignupRequest("doc9@test.com", "password123", "other9"));
        Category category = categoryRepository.findByName("학교").orElseThrow();
        DocumentCreateResponse created = documentService.createDocument(
                new DocumentCreateRequest("삭제 전 제목", "삭제 전 본문", category.getId()),
                author.userId()
        );

        assertThatThrownBy(() -> documentService.deleteDocument(created.documentId(), otherUser.userId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN_DOCUMENT_ACCESS);
    }

    @Test
    @DisplayName("키워드로 제목과 본문을 검색할 수 있다.")
    void searchDocumentsSuccess() {
        SignupResponse user = authService.signup(new SignupRequest("doc10@test.com", "password123", "docUser10"));
        Category schoolCategory = categoryRepository.findByName("학교").orElseThrow();
        Category studyCategory = categoryRepository.findByName("학생").orElseThrow();

        documentService.createDocument(
                new DocumentCreateRequest("진주 학교 생활", "학교 행사 정보", schoolCategory.getId()),
                user.userId()
        );
        documentService.createDocument(
                new DocumentCreateRequest("수학 공부법", "시험 대비 공부 루틴", studyCategory.getId()),
                user.userId()
        );

        PageResponse<DocumentSummaryResponse> response = documentService.searchDocuments("공부", null, 0, 10);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).title()).isEqualTo("수학 공부법");
    }

    @Test
    @DisplayName("검색은 카테고리 필터와 함께 사용할 수 있다.")
    void searchDocumentsWithCategory() {
        SignupResponse user = authService.signup(new SignupRequest("doc11@test.com", "password123", "docUser11"));
        Category schoolCategory = categoryRepository.findByName("학교").orElseThrow();
        Category studyCategory = categoryRepository.findByName("학생").orElseThrow();

        documentService.createDocument(
                new DocumentCreateRequest("학교 시험 정보", "학교 공지", schoolCategory.getId()),
                user.userId()
        );
        documentService.createDocument(
                new DocumentCreateRequest("수학 시험 대비", "공부 계획", studyCategory.getId()),
                user.userId()
        );

        PageResponse<DocumentSummaryResponse> response =
                documentService.searchDocuments("시험", studyCategory.getId(), 0, 10);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).categoryName()).isEqualTo("학생");
    }

    @Test
    @DisplayName("빈 검색어로 검색하면 예외가 발생한다.")
    void searchDocumentsFailWhenKeywordIsBlank() {
        assertThatThrownBy(() -> documentService.searchDocuments("   ", null, 0, 10))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }
}
