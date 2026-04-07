package com.jinju.jinjuwiki.domain.document.dto.response;

import java.time.LocalDateTime;

public record DocumentDetailResponse(
        Long documentId,
        String title,
        String content,
        Long categoryId,
        String categoryName,
        Long authorId,
        String authorNickname,
        Long viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
