package com.jinju.jinjuwiki.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

// Redis 기반 문서 조회수 누적 버퍼 클래스
@Component
@RequiredArgsConstructor
public class RedisDocumentViewCountBuffer {

    private static final String VIEW_COUNT_BUFFER_KEY = "document:view-count:pending";

    private final StringRedisTemplate stringRedisTemplate;

    // 문서 조회수 누적 증가 메서드
    public long increment(Long documentId) {
        Long updatedCount = stringRedisTemplate.opsForHash()
                .increment(VIEW_COUNT_BUFFER_KEY, documentId.toString(), 1L);

        if (updatedCount == null) {
            throw new IllegalStateException("문서 조회수 버퍼 증가 실패");
        }

        return updatedCount;
    }
}
