package com.jinju.jinjuwiki.domain.search.repository;

import com.jinju.jinjuwiki.domain.search.entity.DocumentViewLog;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

// 문서 조회 로그 저장소 인터페이스
public interface DocumentViewLogRepository extends JpaRepository<DocumentViewLog, Long> {

    // 사용자 기준 최근 조회 로그 개수 조회 메서드
    long countByDocumentIdAndUserIdAndCreatedAtAfter(Long documentId, Long userId, LocalDateTime createdAt);

    // IP 기준 최근 조회 로그 개수 조회 메서드
    long countByDocumentIdAndViewerIpAndCreatedAtAfter(Long documentId, String viewerIp, LocalDateTime createdAt);
}
