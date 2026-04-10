package com.jinju.jinjuwiki.domain.search.service;

import com.jinju.jinjuwiki.domain.document.entity.Document;
import org.springframework.stereotype.Component;

// 급상승 문서 후보 정책 컴포넌트
@Component
public class TrendingDocumentCandidatePolicy {

    // 공개, 삭제, 신고 상태 후보 조건 메서드
    public boolean matchesDocumentState(Document document) {
        return true;
    }
}
