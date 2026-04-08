package com.jinju.jinjuwiki.domain.document.mapper;

import com.jinju.jinjuwiki.domain.document.dto.response.DocumentDetailResponse;
import com.jinju.jinjuwiki.domain.document.entity.Document;

public class DocumentDetailResponseMapper {
    private DocumentDetailResponseMapper() {
    }

    // 문서 엔티티를 상세 응답 DTO로 변환
    public static DocumentDetailResponse toResponse(Document document) {
        return new DocumentDetailResponse(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getCategory().getId(),
                document.getCategory().getName(),
                document.getAuthor().getId(),
                document.getAuthor().getNickname(),
                document.getViewCount(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
