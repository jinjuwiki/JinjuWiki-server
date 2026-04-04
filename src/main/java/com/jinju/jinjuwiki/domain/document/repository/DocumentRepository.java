package com.jinju.jinjuwiki.domain.document.repository;

import com.jinju.jinjuwiki.domain.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
