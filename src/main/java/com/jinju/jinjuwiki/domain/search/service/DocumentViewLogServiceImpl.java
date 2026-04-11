package com.jinju.jinjuwiki.domain.search.service;

import com.jinju.jinjuwiki.domain.document.entity.Document;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 문서 조회 로그 저장 서비스 구현체
@Service
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

        // 급상승 집계 Redis 반영 호출
        redisTrendingDocumentViewBuffer.incrementCurrentHourScore(document.getId());
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
}
