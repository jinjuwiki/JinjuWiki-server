package com.jinju.jinjuwiki.domain.document.mapper;

import com.jinju.jinjuwiki.domain.document.dto.response.DocumentCreateResponse;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentDetailResponse;
import com.jinju.jinjuwiki.domain.document.dto.response.DocumentSummaryResponse;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    // 문서 엔티티를 생성 응답 DTO로 변환하는 매퍼다.
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

    // 문서 엔티티를 상세 응답 DTO로 변환하는 매퍼다.
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

    // 문서 엔티티를 목록 응답 DTO로 변환하는 매퍼다.
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
