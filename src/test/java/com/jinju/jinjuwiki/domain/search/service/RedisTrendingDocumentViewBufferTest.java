package com.jinju.jinjuwiki.domain.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

// 급상승 문서 Redis 버퍼 단위 테스트 클래스
@ExtendWith(MockitoExtension.class)
class RedisTrendingDocumentViewBufferTest {

    private static final DateTimeFormatter HOUR_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private RedisTrendingDocumentViewBuffer redisTrendingDocumentViewBuffer;

    @Test
    @DisplayName("현재 시간대 Redis ZSET 점수를 1 증가시킨다.")
    void incrementCurrentHourScoreSuccess() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 4, 10, 22, 0);
        String expectedKey = "search:trending:documents:" + fixedNow.format(HOUR_KEY_FORMATTER);

        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.incrementScore(expectedKey, "10", 1.0D)).thenReturn(4.0D);

        try (MockedStatic<LocalDateTime> mockedLocalDateTime = org.mockito.Mockito.mockStatic(LocalDateTime.class)) {
            mockedLocalDateTime.when(LocalDateTime::now).thenReturn(fixedNow);

            Double updatedScore = redisTrendingDocumentViewBuffer.incrementCurrentHourScore(10L);

            assertThat(updatedScore).isEqualTo(4.0D);
            verify(zSetOperations).incrementScore(expectedKey, "10", 1.0D);
        }
    }
}
