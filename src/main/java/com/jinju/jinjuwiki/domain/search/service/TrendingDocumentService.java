package com.jinju.jinjuwiki.domain.search.service;

import com.jinju.jinjuwiki.domain.search.dto.response.TrendingDocumentsResponse;

// 급상승 문서 조회 서비스 인터페이스
public interface TrendingDocumentService {

    // 급상승 문서 목록 조회 메서드
    TrendingDocumentsResponse getTrendingDocuments();
}
