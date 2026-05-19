package com.jinju.jinjuwiki.domain.document.entity;

import com.jinju.jinjuwiki.domain.category.entity.Category;
import com.jinju.jinjuwiki.domain.user.entity.User;
import com.jinju.jinjuwiki.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// JPA 표준 패턴 기반 문서 Entity 클래스
@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 255)
    private String summary;

    @Column
    private Integer eventYear;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String contentJson;

    @Builder.Default
    @Column(nullable = false)
    private Long viewCount = 0L;

    // LAZY 사용하여 성능 최적화(EAGER 사용시 Category + User 모두 조회)
    // N : 1(Document : User) 관계
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_author_id", nullable = false)
    private User author;

    // 문서 여러개 -> 카테고리 1개
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fk_category_id", nullable = false)
    private Category category;

    // 학교 하위 문서가 어느 학교 문서에 속하는지 연결한다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_school_document_id")
    private Document schoolDocument;

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void update(
            String title,
            String content,
            String summary,
            Integer eventYear,
            String contentJson,
            Category category,
            Document schoolDocument
    ) {
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.eventYear = eventYear;
        this.contentJson = contentJson;
        this.category = category;
        this.schoolDocument = schoolDocument;
    }

    public boolean isWrittenBy(Long userId) {
        return this.author.getId().equals(userId);
    }
}
