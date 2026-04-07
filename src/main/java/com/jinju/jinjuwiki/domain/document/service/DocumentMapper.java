package com.jinju.jinjuwiki.domain.document.service;

import com.jinju.jinjuwiki.domain.document.dto.response.DocumentCreateResponse;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentDetailResponse;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentSummaryResponse;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentCreateResponse toCreateResponse(Document document) {
        return new DocumentCreateResponse(
                document.getId(),
                document.getTitle(),
                document.getContent(),
                document.getCategory().getId(),
                document.getCategory().getName(),
                document.getAuthor().getId(),
                document.getAuthor().getNickname(),
                document.getCreatedAt()
        );
    }

    public DocumentDetailResponse toDetailResponse(Document document) {
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

    public DocumentSummaryResponse toSummaryResponse(Document document) {
        return new DocumentSummaryResponse(
                document.getId(),
                document.getTitle(),
                document.getCategory().getId(),
                document.getCategory().getName(),
                document.getAuthor().getNickname(),
                document.getViewCount(),
                document.getCreatedAt()
        );
    }
}
