package com.jinju.jinjuwiki.domain.document.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jinju.jinjuwiki.domain.category.entity.Category;
import com.jinju.jinjuwiki.domain.category.repository.CategoryRepository;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import com.jinju.jinjuwiki.domain.user.entity.User;
import com.jinju.jinjuwiki.domain.user.entity.UserRole;
import com.jinju.jinjuwiki.domain.user.repository.UserRepository;
import com.jinju.jinjuwiki.global.config.JpaAuditingConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

// 문서 저장소 원자 증가 쿼리 테스트 클래스
@DataJpaTest
@Import(JpaAuditingConfig.class)
class DocumentRepositoryTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    DocumentRepositoryTest(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository
    ) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Test
    @DisplayName("문서 조회수 원자 증가 쿼리는 DB 값 기준으로 조회수를 1 증가시킨다.")
    void incrementViewCountUpdatesDatabaseValueAtomically() {
        // 테스트용 작성자와 카테고리 준비 메서드 호출
        User author = userRepository.save(createUser());
        Category category = categoryRepository.save(createCategory());
        Document savedDocument = documentRepository.saveAndFlush(createDocument(author, category));

        // when
        int updatedCount = documentRepository.incrementViewCount(savedDocument.getId());

        // then
        Document reloadedDocument = documentRepository.findById(savedDocument.getId()).orElseThrow();
        assertThat(updatedCount).isEqualTo(1);
        assertThat(reloadedDocument.getViewCount()).isEqualTo(1L);
    }

    // 테스트용 사용자 생성 메서드
    private User createUser() {
        return User.builder()
                .email("view-count@test.com")
                .password("encoded-password")
                .nickname("viewCounter")
                .role(UserRole.USER)
                .build();
    }

    // 테스트용 카테고리 생성 메서드
    private Category createCategory() {
        return Category.builder()
                .name("조회수")
                .build();
    }

    // 테스트용 문서 생성 메서드
    private Document createDocument(User author, Category category) {
        ObjectNode contentJson = OBJECT_MAPPER.createObjectNode();
        contentJson.put("type", "doc");
        contentJson.putArray("content");

        return Document.builder()
                .title("조회수 테스트 문서")
                .content(contentJson.toString())
                .summary("조회수 테스트 요약")
                .eventYear(2026)
                .contentJson(contentJson.toString())
                .author(author)
                .category(category)
                .build();
    }
}
