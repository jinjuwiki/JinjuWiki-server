package com.jinju.jinjuwiki.domain.document.mapper;

import com.jinju.jinjuwiki.domain.document.dto.response.DocumentCreateResponse;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import org.springframework.stereotype.Component;

@Component
public class DocumentCreateResponseMapper {

    // 생성된 문서 엔티티를 생성 응답 DTO로 변환
    public DocumentCreateResponse toResponse(Document document) {
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
}
