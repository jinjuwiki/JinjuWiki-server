package com.jinju.jinjuwiki.domain.search.service;

import com.jinju.jinjuwiki.global.config.RedisCounterSupport;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// Redis 기반 문서 조회 제한기 클래스
@Component
@RequiredArgsConstructor
public class RedisDocumentViewLimiter {

    private static final long VIEW_LIMIT_PER_HOUR = 3L;
    private static final Duration VIEW_WINDOW = Duration.ofHours(1);
    private static final String VIEW_LIMIT_KEY_PREFIX = "document:view-limit";

    private final RedisCounterSupport redisCounterSupport;

    // 조회 허용 여부 확인 메서드
    public boolean isAllowed(Long documentId, Long viewerUserId, String viewerIp) {
        String redisKey = createRedisKey(documentId, viewerUserId, viewerIp);
        Long currentCount = redisCounterSupport.incrementWithTtl(redisKey, VIEW_WINDOW);

        return currentCount != null && currentCount <= VIEW_LIMIT_PER_HOUR;
    }

    // 조회 제한 Redis 키 생성 메서드
    private String createRedisKey(Long documentId, Long viewerUserId, String viewerIp) {
        if (viewerUserId != null) {
            return VIEW_LIMIT_KEY_PREFIX + ":document:" + documentId + ":user:" + viewerUserId;
        }

        return VIEW_LIMIT_KEY_PREFIX + ":document:" + documentId + ":ip:" + viewerIp;
    }
}
