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
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

// 문서 저장소 원자 증가 쿼리 테스트 클래스
@DataJpaTest
@ActiveProfiles("integration")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaAuditingConfig.class)
class DocumentRepositoryTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EntityManager entityManager;

    @Autowired
    DocumentRepositoryTest(
            DocumentRepository documentRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            EntityManager entityManager
    ) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.entityManager = entityManager;
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
        entityManager.clear();
        Document reloadedDocument = documentRepository.findById(savedDocument.getId()).orElseThrow();
        assertThat(updatedCount).isEqualTo(1);
        assertThat(reloadedDocument.getViewCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("문서 조회수 delta 증가 쿼리는 전달한 값만큼 DB 조회수를 증가시킨다.")
    void incrementViewCountByUpdatesDatabaseValueWithDelta() {
        // 테스트용 작성자와 카테고리 준비 메서드 호출
        User author = userRepository.save(createDeltaUser());
        Category category = categoryRepository.save(createDeltaCategory());
        Document savedDocument = documentRepository.saveAndFlush(createDeltaDocument(author, category));

        // when
        int updatedCount = documentRepository.incrementViewCountBy(savedDocument.getId(), 5L);

        // then
        entityManager.clear();
        Document reloadedDocument = documentRepository.findById(savedDocument.getId()).orElseThrow();
        assertThat(updatedCount).isEqualTo(1);
        assertThat(reloadedDocument.getViewCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("문서 단건 조회는 응답 조립에 필요한 연관 엔티티를 함께 로드한다.")
    void findByIdLoadsRequiredAssociations() {
        User author = userRepository.save(createUser("detail@test.com", "detailUser"));
        Category schoolCategory = categoryRepository.save(createCategory("학교"));
        Category childCategory = categoryRepository.save(createCategory("학생"));
        Document schoolDocument = documentRepository.saveAndFlush(createDocument("진주고등학교", author, schoolCategory, null));
        Document savedDocument = documentRepository.saveAndFlush(createDocument("학생 문서", author, childCategory, schoolDocument));

        entityManager.clear();

        Document foundDocument = documentRepository.findById(savedDocument.getId()).orElseThrow();

        assertThat(Hibernate.isInitialized(foundDocument.getAuthor())).isTrue();
        assertThat(Hibernate.isInitialized(foundDocument.getCategory())).isTrue();
        assertThat(Hibernate.isInitialized(foundDocument.getSchoolDocument())).isTrue();
    }

    @Test
    @DisplayName("문서 목록 조회는 DTO 매핑에 필요한 연관 엔티티를 함께 로드한다.")
    void findByCategoryIdOrderByCreatedAtDescLoadsRequiredAssociations() {
        User author = userRepository.save(createUser("list@test.com", "listUser"));
        Category schoolCategory = categoryRepository.save(createCategory("학교"));
        Category childCategory = categoryRepository.save(createCategory("학생"));
        Document schoolDocument = documentRepository.saveAndFlush(createDocument("진주여고", author, schoolCategory, null));
        documentRepository.saveAndFlush(createDocument("학생 문서 1", author, childCategory, schoolDocument));
        documentRepository.saveAndFlush(createDocument("학생 문서 2", author, childCategory, schoolDocument));

        entityManager.clear();

        Page<Document> documents = documentRepository.findByCategoryIdOrderByCreatedAtDesc(
                childCategory.getId(),
                PageRequest.of(0, 10)
        );

        assertThat(documents.getContent()).hasSize(2);
        assertThat(documents.getContent())
                .allSatisfy(document -> {
                    assertThat(Hibernate.isInitialized(document.getAuthor())).isTrue();
                    assertThat(Hibernate.isInitialized(document.getCategory())).isTrue();
                    assertThat(Hibernate.isInitialized(document.getSchoolDocument())).isTrue();
                });
    }

    @Test
    @DisplayName("문서 검색 조회는 DTO 매핑에 필요한 연관 엔티티를 함께 로드한다.")
    void searchByKeywordLoadsRequiredAssociations() {
        User author = userRepository.save(createUser("search@test.com", "searchUser"));
        Category schoolCategory = categoryRepository.save(createCategory("학교"));
        Category childCategory = categoryRepository.save(createCategory("사건사고"));
        Document schoolDocument = documentRepository.saveAndFlush(createDocument("진주중앙고", author, schoolCategory, null));
        documentRepository.saveAndFlush(createDocument("축제 사고 문서", author, childCategory, schoolDocument));

        entityManager.clear();

        Page<Document> documents = documentRepository.searchByKeyword("사고", PageRequest.of(0, 10));

        assertThat(documents.getContent()).hasSize(1);
        Document foundDocument = documents.getContent().getFirst();
        assertThat(Hibernate.isInitialized(foundDocument.getAuthor())).isTrue();
        assertThat(Hibernate.isInitialized(foundDocument.getCategory())).isTrue();
        assertThat(Hibernate.isInitialized(foundDocument.getSchoolDocument())).isTrue();
    }

    // 테스트용 사용자 생성 메서드
    private User createUser() {
        return createUser("view-count@test.com", "viewCounter");
    }

    private User createUser(String email, String nickname) {
        return User.builder()
                .email(email)
                .password("encoded-password")
                .nickname(nickname)
                .role(UserRole.USER)
                .build();
    }

    // 테스트용 카테고리 생성 메서드
    private Category createCategory() {
        return createCategory("조회수");
    }

    private Category createCategory(String name) {
        return Category.builder()
                .name(name)
                .build();
    }

    // delta 테스트용 카테고리 생성 메서드
    private Category createDeltaCategory() {
        return Category.builder()
                .name("조회수-delta")
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

    // delta 테스트용 사용자 생성 메서드
    private User createDeltaUser() {
        return User.builder()
                .email("view-count-delta@test.com")
                .password("encoded-password")
                .nickname("viewCounterDelta")
                .role(UserRole.USER)
                .build();
    }

    // delta 테스트용 문서 생성 메서드
    private Document createDeltaDocument(User author, Category category) {
        return createDocument("조회수 delta 테스트 문서", author, category, null);
    }

    private Document createDocument(String title, User author, Category category, Document schoolDocument) {
        ObjectNode contentJson = OBJECT_MAPPER.createObjectNode();
        contentJson.put("type", "doc");
        contentJson.putArray("content");

        return Document.builder()
                .title(title)
                .content(contentJson.toString())
                .summary("조회수 delta 테스트 요약")
                .eventYear(2026)
                .contentJson(contentJson.toString())
                .author(author)
                .category(category)
                .schoolDocument(schoolDocument)
                .build();
    }
}
