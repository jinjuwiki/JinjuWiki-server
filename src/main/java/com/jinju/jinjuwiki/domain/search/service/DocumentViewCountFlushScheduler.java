package com.jinju.jinjuwiki.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 문서 조회수 flush 주기 실행 스케줄러 클래스
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "app.search",
        name = "view-count-flush-enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class DocumentViewCountFlushScheduler {

    private final DocumentViewCountFlushService documentViewCountFlushService;

    // 문서 조회수 flush 스케줄 호출 메서드
    @Scheduled(fixedDelayString = "${app.search.view-count-flush-delay-millis:60000}")
    public void flushPendingViewCounts() {
        documentViewCountFlushService.flushPendingViewCounts();
    }
}
