package com.jinju.jinjuwiki.domain.search.service;

import com.jinju.jinjuwiki.domain.document.entity.Document;

// 문서 조회 로그 저장 서비스 인터페이스
public interface DocumentViewLogService {

    // 문서 조회 로그 반영 메서드
    boolean save(Document document, Long viewerUserId, String viewerIp);
}
