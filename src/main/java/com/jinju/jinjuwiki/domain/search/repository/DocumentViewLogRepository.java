package com.jinju.jinjuwiki.domain.search.repository;

import com.jinju.jinjuwiki.domain.search.entity.DocumentViewLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 문서 조회 로그 저장소 인터페이스
public interface DocumentViewLogRepository extends JpaRepository<DocumentViewLog, Long> {

    // 급상승 문서 집계 projection 인터페이스
    interface TrendingDocumentProjection {
        Long getDocumentId();
        long getViewCount();
        LocalDateTime getLastViewedAt();
    }

    // 사용자 기준 최근 조회 로그 개수 조회 메서드
    long countByDocumentIdAndUserIdAndCreatedAtAfter(Long documentId, Long userId, LocalDateTime createdAt);

    // IP 기준 최근 조회 로그 개수 조회 메서드
    long countByDocumentIdAndViewerIpAndCreatedAtAfter(Long documentId, String viewerIp, LocalDateTime createdAt);

    // 최근 1시간 유효 조회 집계 조회 메서드
    @Query("""
            select l.document.id as documentId,
                   count(l.id) as viewCount,
                   max(l.createdAt) as lastViewedAt
            from DocumentViewLog l
            where l.createdAt >= :since
            group by l.document.id
            order by count(l.id) desc
            """)
    List<TrendingDocumentProjection> findTrendingDocumentsSince(@Param("since") LocalDateTime since);
}
