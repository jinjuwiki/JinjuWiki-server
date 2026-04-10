package com.jinju.jinjuwiki.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 문서 조회수 flush 주기 실행 스케줄러 클래스
@Component
@RequiredArgsConstructor
public class DocumentViewCountFlushScheduler {

    private static final long VIEW_COUNT_FLUSH_DELAY_MILLIS = 60_000L;

    private final DocumentViewCountFlushService documentViewCountFlushService;

    // 문서 조회수 flush 스케줄 호출 메서드
    @Scheduled(fixedDelay = VIEW_COUNT_FLUSH_DELAY_MILLIS)
    public void flushPendingViewCounts() {
        documentViewCountFlushService.flushPendingViewCounts();
    }
}
