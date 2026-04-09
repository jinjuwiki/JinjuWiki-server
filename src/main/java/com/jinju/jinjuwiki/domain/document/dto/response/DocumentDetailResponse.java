package com.jinju.jinjuwiki.domain.document.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;

public record DocumentDetailResponse(
        Long documentId,
        String title,
        String summary,
        Long categoryId,
        String categoryName,
        Integer eventYear,
        Long authorId,
        String authorNickname,
        JsonNode contentJson,
        Long viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
