package com.jinju.jinjuwiki.domain.document.service;

import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationSendRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationVerifyRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jinju.jinjuwiki.domain.auth.dto.request.SignupRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.SignupResponse;
import com.jinju.jinjuwiki.domain.auth.repository.EmailVerificationRepository;
import com.jinju.jinjuwiki.domain.auth.service.AuthService;
import com.jinju.jinjuwiki.domain.auth.service.EmailSender;
import com.jinju.jinjuwiki.domain.category.entity.Category;
import com.jinju.jinjuwiki.domain.category.repository.CategoryRepository;
import com.jinju.jinjuwiki.domain.document.dto.request.DocumentCreateRequest;
import com.jinju.jinjuwiki.domain.document.dto.request.DocumentUpdateRequest;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import com.jinju.jinjuwiki.domain.document.repository.DocumentRepository;

@Transactional
@SpringBootTest
class DocumentServiceTest {

    private static final String SCHOOL = "학교";
    private static final String STUDENT = "학생";
    private static final String INCIDENT = "사건사고";
    private static final String TEACHER = "선생님";
    private static final String ETC = "기타";

    private final DocumentService documentService;
    private final AuthService authService;
    private final CategoryRepository categoryRepository;
    private final DocumentRepository documentRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    @Autowired
    DocumentServiceTest(
            DocumentService documentService,
            AuthService authService,
            CategoryRepository categoryRepository,
            DocumentRepository documentRepository,
            EmailVerificationRepository emailVerificationRepository
    ) {
        this.documentService = documentService;
        this.authService = authService;
        this.categoryRepository = categoryRepository;
        this.documentRepository = documentRepository;
        this.emailVerificationRepository = emailVerificationRepository;
    }

    @TestConfiguration
    static class TestMailConfig {

        @Bean
        @Primary
        EmailSender emailSender() {
            return (to, code) -> {
            };
        }
    }

    @Test
    @DisplayName("문서를 생성하면 작성자와 카테고리 정보가 함께 반환된다.")
    void createDocumentSuccess() {
        // given
        verifyEmail("doc1@test.com");
        SignupResponse user = authService.signup(new SignupRequest("doc1@test.com", "password123", "docUser"));
        Category category = getCategory(SCHOOL);

        // when
        Document response = documentService.createDocument(
                new DocumentCreateRequest("문서 제목", "문서 본문", category.getId()),
                user.userId()
        );

        // then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getAuthor().getNickname()).isEqualTo("docUser");
        assertThat(response.getCategory().getName()).isEqualTo("학교");
    }

    @Test
    @DisplayName("존재하지 않는 작성자 ID로 문서를 생성하면 예외가 발생한다.")
    void createDocumentFailWhenUserNotFound() {
        // given
        Category category = getCategory(SCHOOL);

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> documentService.createDocument(
                new DocumentCreateRequest("문서 제목", "문서 본문", category.getId()),
                999L
        ));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("문서를 조회하면 조회수가 증가한다.")
    void getDocumentSuccess() {
        // given
        verifyEmail("doc2@test.com");
        SignupResponse user = authService.signup(new SignupRequest("doc2@test.com", "password123", "docUser2"));
        Category category = getCategory(STUDENT);
        Document created = documentService.createDocument(
                new DocumentCreateRequest("수학 공부법", "개념부터 반복", category.getId()),
                user.userId()
        );

        // when
        Document response = documentService.getDocument(created.getId());

        // then
        assertThat(response.getId()).isEqualTo(created.getId());
        assertThat(response.getViewCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("문서 목록은 최신순으로 페이지 조회할 수 있다.")
    void getDocumentsSuccess() {
        // given
        verifyEmail("doc3@test.com");
        SignupResponse user = authService.signup(new SignupRequest("doc3@test.com", "password123", "docUser3"));
        Category category = getCategory(INCIDENT);

        documentService.createDocument(new DocumentCreateRequest("첫 번째 글", "내용 1", category.getId()), user.userId());
        documentService.createDocument(new DocumentCreateRequest("두 번째 글", "내용 2", category.getId()), user.userId());

        // when
        Page<Document> response = documentService.getDocuments(category.getId(), 0, 10);

        // then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("두 번째 글");
    }

    @Test
    @DisplayName("작성자는 자신의 문서를 수정할 수 있다.")
    void updateDocumentSuccess() {
        // given
        verifyEmail("doc4@test.com");
        SignupResponse user = authService.signup(new SignupRequest("doc4@test.com", "password123", "docUser4"));
        Category category = getCategory(SCHOOL);
        Category updatedCategory = getCategory(TEACHER);
        Document created = documentService.createDocument(
                new DocumentCreateRequest("원래 제목", "원래 본문", category.getId()),
                user.userId()
        );

        // when
        Document response = documentService.updateDocument(
                created.getId(),
                new DocumentUpdateRequest("수정 제목", "수정 본문", updatedCategory.getId()),
                user.userId()
        );

        // then
        assertThat(response.getTitle()).isEqualTo("수정 제목");
        assertThat(response.getContent()).isEqualTo("수정 본문");
        assertThat(response.getCategory().getName()).isEqualTo(TEACHER);
    }

    @Test
    @DisplayName("작성자가 아니면 문서를 수정할 수 없다.")
    void updateDocumentFailWhenNotAuthor() {
        // given
        verifyEmail("doc5@test.com");
        SignupResponse author = authService.signup(new SignupRequest("doc5@test.com", "password123", "author5"));
        verifyEmail("doc6@test.com");
        SignupResponse otherUser = authService.signup(new SignupRequest("doc6@test.com", "password123", "other6"));
        Category category = getCategory(SCHOOL);
        Document created = documentService.createDocument(
                new DocumentCreateRequest("제목", "본문", category.getId()),
                author.userId()
        );

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> documentService.updateDocument(
                created.getId(),
                new DocumentUpdateRequest("수정 제목", "수정 본문", category.getId()),
                otherUser.userId()
        ));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_DOCUMENT_ACCESS);
    }

    @Test
    @DisplayName("작성자는 자신의 문서를 삭제할 수 있다.")
    void deleteDocumentSuccess() {
        // given
        verifyEmail("doc7@test.com");
        SignupResponse user = authService.signup(new SignupRequest("doc7@test.com", "password123", "docUser7"));
        Category category = getCategory(ETC);
        Document created = documentService.createDocument(
                new DocumentCreateRequest("삭제 제목", "삭제 본문", category.getId()),
                user.userId()
        );

        // when
        documentService.deleteDocument(created.getId(), user.userId());

        // then
        assertThat(documentRepository.findById(created.getId())).isEmpty();
    }

    @Test
    @DisplayName("작성자가 아니면 문서를 삭제할 수 없다.")
    void deleteDocumentFailWhenNotAuthor() {
        // given
        verifyEmail("doc8@test.com");
        SignupResponse author = authService.signup(new SignupRequest("doc8@test.com", "password123", "author8"));
        verifyEmail("doc9@test.com");
        SignupResponse otherUser = authService.signup(new SignupRequest("doc9@test.com", "password123", "other9"));
        Category category = getCategory(SCHOOL);
        Document created = documentService.createDocument(
                new DocumentCreateRequest("삭제 전 제목", "삭제 전 본문", category.getId()),
                author.userId()
        );

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> documentService.deleteDocument(created.getId(), otherUser.userId())
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_DOCUMENT_ACCESS);
    }

    @Test
    @DisplayName("키워드로 제목과 본문을 검색할 수 있다.")
    void searchDocumentsSuccess() {
        // given
        verifyEmail("doc10@test.com");
        SignupResponse user = authService.signup(new SignupRequest("doc10@test.com", "password123", "docUser10"));
        Category schoolCategory = getCategory(SCHOOL);
        Category studyCategory = getCategory(STUDENT);

        documentService.createDocument(
                new DocumentCreateRequest("진주 학교 생활", "학교 행사 정보", schoolCategory.getId()),
                user.userId()
        );
        documentService.createDocument(
                new DocumentCreateRequest("수학 공부법", "시험 대비 공부 루틴", studyCategory.getId()),
                user.userId()
        );

        // when
        Page<Document> response = documentService.searchDocuments("공부", null, 0, 10);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("수학 공부법");
    }

    @Test
    @DisplayName("검색은 카테고리 필터와 함께 사용할 수 있다.")
    void searchDocumentsWithCategory() {
        // given
        verifyEmail("doc11@test.com");
        SignupResponse user = authService.signup(new SignupRequest("doc11@test.com", "password123", "docUser11"));
        Category schoolCategory = getCategory(SCHOOL);
        Category studyCategory = getCategory(STUDENT);

        documentService.createDocument(
                new DocumentCreateRequest("학교 시험 정보", "학교 공지", schoolCategory.getId()),
                user.userId()
        );
        documentService.createDocument(
                new DocumentCreateRequest("수학 시험 대비", "공부 계획", studyCategory.getId()),
                user.userId()
        );

        // when
        Page<Document> response =
                documentService.searchDocuments("시험", studyCategory.getId(), 0, 10);

        // then
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getCategory().getName()).isEqualTo(STUDENT);
    }

    @Test
    @DisplayName("빈 검색어로 검색하면 예외가 발생한다.")
    void searchDocumentsFailWhenKeywordIsBlank() {
        // given
        String keyword = "   ";

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> documentService.searchDocuments(keyword, null, 0, 10)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT);
    }

    private void verifyEmail(String email) {
        authService.sendVerificationCode(new EmailVerificationSendRequest(email));
        String code = emailVerificationRepository.findByEmail(email).orElseThrow().getCode();
        authService.verifyCode(new EmailVerificationVerifyRequest(email, code));
    }

    private Category getCategory(String categoryName) {
        return categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new AssertionError("테스트용 카테고리를 찾을 수 없습니다: " + categoryName));
    }
}
