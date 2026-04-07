package com.jinju.jinjuwiki.domain.document.dto.response;

import java.time.LocalDateTime;

public record DocumentSummaryResponse(
        Long documentId,
        String title,
        Long categoryId,
        String categoryName,
        String authorNickname,
        Long viewCount,
        LocalDateTime createdAt
) {
}
