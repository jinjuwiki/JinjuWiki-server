package com.jinju.jinjuwiki.domain.search.repository;

import com.jinju.jinjuwiki.domain.search.entity.DocumentViewLog;
import org.springframework.data.jpa.repository.JpaRepository;

// 문서 조회 로그 저장소 인터페이스
public interface DocumentViewLogRepository extends JpaRepository<DocumentViewLog, Long> {
}
