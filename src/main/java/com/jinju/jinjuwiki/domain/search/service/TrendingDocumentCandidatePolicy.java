package com.jinju.jinjuwiki.domain.search.service;

import com.jinju.jinjuwiki.domain.document.entity.Document;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

// 급상승 문서 후보 정책 컴포넌트
@Component
public class TrendingDocumentCandidatePolicy {

    private static final long DOCUMENT_MINIMUM_AGE_HOURS = 3L;

    // 급상승 문서 후보 조건 확인 메서드
    public boolean matchesDocumentState(Document document) {
        return document != null
                && hasRequiredDocumentData(document)
                && isOlderThanMinimumAge(document)
                && isPublicDocument(document)
                && isNotDeletedDocument(document)
                && isNotUnderReview(document);
    }

    // 급상승 문서 필수 데이터 확인 메서드
    private boolean hasRequiredDocumentData(Document document) {
        return document.getTitle() != null
                && !document.getTitle().isBlank()
                && document.getAuthor() != null
                && document.getCategory() != null;
    }

    // 문서 생성 3시간 경과 조건 메서드
    private boolean isOlderThanMinimumAge(Document document) {
        return document.getCreatedAt() != null
                && document.getCreatedAt().isBefore(LocalDateTime.now().minusHours(DOCUMENT_MINIMUM_AGE_HOURS));
    }

    // 공개 문서 조건 메서드
    private boolean isPublicDocument(Document document) {
        // 현재 문서 엔티티에 공개 여부 필드가 없어 후속 상태 필드 연결 지점으로 유지하는 조건
        return true;
    }

    // 삭제 제외 조건 메서드
    private boolean isNotDeletedDocument(Document document) {
        // 현재 문서 엔티티에 삭제 상태 필드가 없어 후속 상태 필드 연결 지점으로 유지하는 조건
        return true;
    }

    // 신고 검토 제외 조건 메서드
    private boolean isNotUnderReview(Document document) {
        // 현재 문서 엔티티에 신고 검토 상태 필드가 없어 후속 상태 필드 연결 지점으로 유지하는 조건
        return true;
    }
}
