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
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import com.jinju.jinjuwiki.global.response.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class DocumentServiceTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private AuthService authService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("문서를 생성하면 작성자와 카테고리 정보가 함께 반환된다.")
    void createDocumentSuccess() {
        SignupResponse user = authService.signup(new SignupRequest("doc1@test.com", "password123", "docUser"));
        Category category = categoryRepository.findByName("학교").orElseThrow();

        DocumentCreateResponse response = documentService.createDocument(
                new DocumentCreateRequest("문서 제목", "문서 본문", category.getId(), user.userId())
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
                new DocumentCreateRequest("문서 제목", "문서 본문", category.getId(), 999L)
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("문서를 조회하면 조회수가 증가한다.")
    void getDocumentSuccess() {
        SignupResponse user = authService.signup(new SignupRequest("doc2@test.com", "password123", "docUser2"));
        Category category = categoryRepository.findByName("공부").orElseThrow();
        DocumentCreateResponse created = documentService.createDocument(
                new DocumentCreateRequest("수학 공부법", "개념부터 반복", category.getId(), user.userId())
        );

        DocumentDetailResponse response = documentService.getDocument(created.documentId());

        assertThat(response.documentId()).isEqualTo(created.documentId());
        assertThat(response.viewCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("문서 목록은 최신순으로 페이지 조회할 수 있다.")
    void getDocumentsSuccess() {
        SignupResponse user = authService.signup(new SignupRequest("doc3@test.com", "password123", "docUser3"));
        Category category = categoryRepository.findByName("생활").orElseThrow();

        documentService.createDocument(new DocumentCreateRequest("첫 번째 글", "내용 1", category.getId(), user.userId()));
        documentService.createDocument(new DocumentCreateRequest("두 번째 글", "내용 2", category.getId(), user.userId()));

        PageResponse<DocumentSummaryResponse> response = documentService.getDocuments(category.getId(), 0, 10);

        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).title()).isEqualTo("두 번째 글");
    }
}
