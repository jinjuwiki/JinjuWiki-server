package com.jinju.jinjuwiki.domain.document.dto;

import java.time.LocalDateTime;

public record DocumentCreateResponse(
        Long documentId,
        String title,
        String content,
        Long categoryId,
        String categoryName,
        Long authorId,
        String authorNickname,
        LocalDateTime createdAt
) {
}
