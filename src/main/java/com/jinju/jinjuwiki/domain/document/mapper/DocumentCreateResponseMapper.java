package com.jinju.jinjuwiki.domain.document.mapper;

import com.jinju.jinjuwiki.domain.document.dto.response.DocumentCreateResponse;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import com.jinju.jinjuwiki.domain.document.support.DocumentContentJsonCodec;

public class DocumentCreateResponseMapper {
    private DocumentCreateResponseMapper() {
    }

    // 생성된 문서 엔티티를 생성 응답 DTO로 변환
    public static DocumentCreateResponse toResponse(Document document) {
        return new DocumentCreateResponse(
                document.getId(),
                document.getTitle(),
                document.getSummary(),
                document.getCategory().getId(),
                document.getCategory().getName(),
                document.getEventYear(),
                document.getAuthor().getId(),
                document.getAuthor().getNickname(),
                DocumentContentJsonCodec.readTree(document.getContentJson() == null ? document.getContent() : document.getContentJson()),
                document.getCreatedAt()
        );
    }
}
