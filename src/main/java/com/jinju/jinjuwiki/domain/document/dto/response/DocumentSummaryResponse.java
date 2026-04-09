package com.jinju.jinjuwiki.domain.document.dto.response;

import java.time.LocalDateTime;

public record DocumentSummaryResponse(
        Long documentId,
        String title,
        String summary,
        Long categoryId,
        String categoryName,
        Integer eventYear,
        String authorNickname,
        Long viewCount,
        LocalDateTime createdAt
) {
}
