package com.jinju.jinjuwiki.domain.document.mapper;

import com.jinju.jinjuwiki.domain.document.dto.response.DocumentDetailResponse;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import com.jinju.jinjuwiki.domain.document.support.DocumentContentJsonCodec;

public class DocumentDetailResponseMapper {
    private DocumentDetailResponseMapper() {
    }

    // 문서 엔티티를 상세 응답 DTO로 변환
    public static DocumentDetailResponse toResponse(Document document) {
        return toResponse(document, document.getViewCount());
    }

    // 문서 엔티티를 상세 응답 DTO로 변환
    public static DocumentDetailResponse toResponse(Document document, long viewCount) {
        return new DocumentDetailResponse(
                document.getId(),
                document.getTitle(),
                document.getSummary(),
                document.getCategory().getId(),
                document.getCategory().getName(),
                document.getSchoolDocument() == null ? null : document.getSchoolDocument().getId(),
                document.getSchoolDocument() == null ? null : document.getSchoolDocument().getTitle(),
                document.getEventYear(),
                document.getAuthor().getId(),
                document.getAuthor().getNickname(),
                DocumentContentJsonCodec.readTree(document.getContentJson() == null ? document.getContent() : document.getContentJson()),
                viewCount,
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
}
