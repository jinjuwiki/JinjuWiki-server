package com.jinju.jinjuwiki.domain.document.repository;

import com.jinju.jinjuwiki.domain.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Page<Document> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Document> findByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);

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
}
