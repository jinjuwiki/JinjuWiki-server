package com.jinju.jinjuwiki.domain.document.repository;

import com.jinju.jinjuwiki.domain.document.entity.Document;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Override
    @EntityGraph(attributePaths = {"author", "category", "schoolDocument"})
    Optional<Document> findById(Long id);

    @EntityGraph(attributePaths = {"author", "category", "schoolDocument"})
    Page<Document> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category", "schoolDocument"})
    Page<Document> findByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category", "schoolDocument"})
    Page<Document> findBySchoolDocumentIdOrderByCreatedAtDesc(Long schoolDocumentId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category", "schoolDocument"})
    Page<Document> findByCategoryIdAndSchoolDocumentIdOrderByCreatedAtDesc(Long categoryId, Long schoolDocumentId, Pageable pageable);

    List<Document> findByCategoryIdOrderByTitleAsc(Long categoryId);

    boolean existsBySchoolDocumentId(Long schoolDocumentId);

    // 조회수 원자 증가 쿼리
    @Modifying(flushAutomatically = true)
    @Query("""
            update Document d
            set d.viewCount = d.viewCount + 1
            where d.id = :documentId
            """)
    int incrementViewCount(@Param("documentId") Long documentId);

    // 조회수 delta 원자 증가 쿼리
    @Modifying(flushAutomatically = true)
    @Query("""
            update Document d
            set d.viewCount = d.viewCount + :delta
            where d.id = :documentId
            """)
    int incrementViewCountBy(@Param("documentId") Long documentId, @Param("delta") long delta);

    // concat() : 두개 이상의 컬럼이나 문자열을 순서대로 묶어 하나의 문자열로 반환
    @EntityGraph(attributePaths = {"author", "category", "schoolDocument"})
    @Query("""
            select d
            from Document d
            where lower(d.title) like lower(concat('%', :keyword, '%'))
              or lower(d.summary) like lower(concat('%', :keyword, '%'))
            order by d.createdAt desc
            """)
    Page<Document> searchByKeyword(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"author", "category", "schoolDocument"})
    @Query("""
            select d
            from Document d
            where d.category.id = :categoryId
              and (
                  lower(d.title) like lower(concat('%', :keyword, '%'))
                or lower(d.summary) like lower(concat('%', :keyword, '%'))
              )
            order by d.createdAt desc
            """)
    Page<Document> searchByCategoryAndKeyword(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"author", "category", "schoolDocument"})
    @Query("""
            select d
            from Document d
            where d.schoolDocument.id = :schoolDocumentId
              and (
                  lower(d.title) like lower(concat('%', :keyword, '%'))
                or lower(d.summary) like lower(concat('%', :keyword, '%'))
              )
            order by d.createdAt desc
            """)
    Page<Document> searchBySchoolAndKeyword(
            @Param("schoolDocumentId") Long schoolDocumentId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"author", "category", "schoolDocument"})
    @Query("""
            select d
            from Document d
            where d.category.id = :categoryId
              and d.schoolDocument.id = :schoolDocumentId
              and (
                  lower(d.title) like lower(concat('%', :keyword, '%'))
                or lower(d.summary) like lower(concat('%', :keyword, '%'))
              )
            order by d.createdAt desc
            """)
    Page<Document> searchByCategoryAndSchoolAndKeyword(
            @Param("categoryId") Long categoryId,
            @Param("schoolDocumentId") Long schoolDocumentId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
