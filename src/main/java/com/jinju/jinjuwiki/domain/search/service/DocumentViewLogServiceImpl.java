package com.jinju.jinjuwiki.domain.search.service;

import com.jinju.jinjuwiki.domain.document.entity.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 문서 조회 로그 저장 서비스 구현체
@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentViewLogServiceImpl implements DocumentViewLogService {

    private final RedisDocumentViewLimiter redisDocumentViewLimiter;
    private final RedisTrendingDocumentViewBuffer redisTrendingDocumentViewBuffer;

    @Override
    @Transactional
    public boolean save(Document document, Long viewerUserId, String viewerIp) {
        if (!isAllowedDocumentView(document, viewerUserId, viewerIp)) {
            return false;
        }

        recordTrendingDocumentView(document);
        return true;
    }

    // 조회 제한 확인 메서드
    private boolean isAllowedDocumentView(Document document, Long viewerUserId, String viewerIp) {
        if (viewerUserId == null && isBlankIp(viewerIp)) {
            return false;
        }

        try {
            return redisDocumentViewLimiter.isAllowed(document.getId(), viewerUserId, viewerIp);
        } catch (RuntimeException exception) {
            // Redis 장애 시 문서 조회 API 보호용 fail-open 정책
            return true;
        }
    }

    // 비로그인 조회 IP 공백 여부 확인 메서드
    private boolean isBlankIp(String viewerIp) {
        return viewerIp == null || viewerIp.isBlank();
    }

    // 급상승 집계 Redis 반영 메서드
    private void recordTrendingDocumentView(Document document) {
        try {
            redisTrendingDocumentViewBuffer.incrementCurrentHourScore(document.getId());
        } catch (RuntimeException exception) {
            // 급상승 집계 실패가 문서 조회 성공을 막지 않도록 한다.
            log.warn("급상승 문서 집계 반영 실패, documentId={}", document.getId(), exception);
        }
    }
}
