package com.jinju.jinjuwiki.domain.document.mapper;

import com.jinju.jinjuwiki.domain.document.dto.response.DocumentSummaryResponse;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentSummaryResponseMapper {

    // 문서 엔티티를 목록 응답 DTO로 변환
    public DocumentSummaryResponse toResponse(Document document) {
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
