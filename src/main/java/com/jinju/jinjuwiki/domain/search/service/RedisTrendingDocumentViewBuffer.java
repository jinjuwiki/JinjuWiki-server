package com.jinju.jinjuwiki.domain.search.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

// 급상승 문서 Redis 시간대별 집계 버퍼 클래스
@Component
@RequiredArgsConstructor
public class RedisTrendingDocumentViewBuffer {

    private static final String TRENDING_KEY_PREFIX = "search:trending:documents";
    private static final DateTimeFormatter HOUR_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");

    private final StringRedisTemplate stringRedisTemplate;

    // 현재 시간대 급상승 점수 증가 메서드
    public Double incrementCurrentHourScore(Long documentId) {
        String trendingKey = createCurrentHourKey(LocalDateTime.now());
        ZSetOperations<String, String> zSetOperations = stringRedisTemplate.opsForZSet();

        return zSetOperations.incrementScore(trendingKey, documentId.toString(), 1.0D);
    }

    // 시간대별 급상승 Redis 키 생성 메서드
    private String createCurrentHourKey(LocalDateTime now) {
        return TRENDING_KEY_PREFIX + ":" + now.format(HOUR_KEY_FORMATTER);
    }
}
