package com.jinju.jinjuwiki.domain.document.repository;

import com.jinju.jinjuwiki.domain.document.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    Page<Document> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Document> findByCategoryIdOrderByCreatedAtDesc(Long categoryId, Pageable pageable);
}
