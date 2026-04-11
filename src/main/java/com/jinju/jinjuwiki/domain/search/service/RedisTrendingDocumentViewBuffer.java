package com.jinju.jinjuwiki.domain.search.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

// 급상승 문서 Redis 시간대별 집계 버퍼 클래스
@Component
@RequiredArgsConstructor
public class RedisTrendingDocumentViewBuffer {

    private static final int TRENDING_FETCH_LIMIT = 20;
    private static final String TRENDING_KEY_PREFIX = "search:trending:documents";
    private static final DateTimeFormatter HOUR_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");

    private final StringRedisTemplate stringRedisTemplate;

    // 현재 시간대 급상승 점수 증가 메서드
    public Double incrementCurrentHourScore(Long documentId) {
        String trendingKey = createCurrentHourKey(LocalDateTime.now());
        ZSetOperations<String, String> zSetOperations = stringRedisTemplate.opsForZSet();

        return zSetOperations.incrementScore(trendingKey, documentId.toString(), 1.0D);
    }

    // 최근 1시간 급상승 문서 ID 조회 메서드
    public List<Long> findTrendingDocumentIds() {
        ZSetOperations<String, String> zSetOperations = stringRedisTemplate.opsForZSet();
        LocalDateTime now = LocalDateTime.now();
        Map<Long, Double> aggregatedScores = new HashMap<>();

        mergeScores(aggregatedScores, zSetOperations.reverseRangeWithScores(createCurrentHourKey(now), 0, TRENDING_FETCH_LIMIT - 1));
        mergeScores(aggregatedScores, zSetOperations.reverseRangeWithScores(createCurrentHourKey(now.minusHours(1)), 0, TRENDING_FETCH_LIMIT - 1));

        return aggregatedScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(TRENDING_FETCH_LIMIT)
                .map(Map.Entry::getKey)
                .toList();
    }

    // 시간대별 급상승 Redis 키 생성 메서드
    private String createCurrentHourKey(LocalDateTime now) {
        return TRENDING_KEY_PREFIX + ":" + now.format(HOUR_KEY_FORMATTER);
    }

    // Redis ZSET 점수 병합 메서드
    private void mergeScores(
            Map<Long, Double> aggregatedScores,
            java.util.Set<ZSetOperations.TypedTuple<String>> tuples
    ) {
        if (tuples == null || tuples.isEmpty()) {
            return;
        }

        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            if (tuple.getValue() == null || tuple.getScore() == null) {
                continue;
            }

            aggregatedScores.merge(Long.parseLong(tuple.getValue()), tuple.getScore(), Double::sum);
        }
    }
}
