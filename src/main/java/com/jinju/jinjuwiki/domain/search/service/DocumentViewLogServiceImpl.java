package com.jinju.jinjuwiki.domain.search.service;

import com.jinju.jinjuwiki.domain.document.entity.Document;
import com.jinju.jinjuwiki.domain.search.entity.DocumentViewLog;
import com.jinju.jinjuwiki.domain.search.repository.DocumentViewLogRepository;
import com.jinju.jinjuwiki.domain.user.entity.User;
import com.jinju.jinjuwiki.domain.user.repository.UserRepository;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 문서 조회 로그 저장 서비스 구현체
@Service
@RequiredArgsConstructor
public class DocumentViewLogServiceImpl implements DocumentViewLogService {

    private static final long VIEW_LIMIT_PER_HOUR = 3L;

    private final DocumentViewLogRepository documentViewLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void save(Document document, Long viewerUserId, String viewerIp) {
        if (hasExceededUserViewLimit(document, viewerUserId)) {
            return;
        }

        if (hasExceededIpViewLimit(document, viewerUserId, viewerIp)) {
            return;
        }

        User viewer = resolveViewer(viewerUserId);

        documentViewLogRepository.save(DocumentViewLog.builder()
                .document(document)
                .user(viewer)
                .viewerIp(viewerIp)
                .build());
    }

    // 로그인 사용자 조회자 조회 메서드
    private User resolveViewer(Long viewerUserId) {
        if (viewerUserId == null) {
            return null;
        }

        return userRepository.findById(viewerUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    // 로그인 사용자 조회 제한 확인 메서드
    private boolean hasExceededUserViewLimit(Document document, Long viewerUserId) {
        if (viewerUserId == null) {
            return false;
        }

        long viewCount = documentViewLogRepository.countByDocumentIdAndUserIdAndCreatedAtAfter(
                document.getId(),
                viewerUserId,
                getOneHourAgo()
        );
        return viewCount >= VIEW_LIMIT_PER_HOUR;
    }

    // 비로그인 사용자 조회 제한 확인 메서드
    private boolean hasExceededIpViewLimit(Document document, Long viewerUserId, String viewerIp) {
        if (viewerUserId != null || isBlankIp(viewerIp)) {
            return false;
        }

        long viewCount = documentViewLogRepository.countByDocumentIdAndViewerIpAndCreatedAtAfter(
                document.getId(),
                viewerIp,
                getOneHourAgo()
        );
        return viewCount >= VIEW_LIMIT_PER_HOUR;
    }

    // 최근 1시간 기준 시각 생성 메서드
    private LocalDateTime getOneHourAgo() {
        return LocalDateTime.now().minusHours(1);
    }

    // 비로그인 조회 IP 공백 여부 확인 메서드
    private boolean isBlankIp(String viewerIp) {
        return viewerIp == null || viewerIp.isBlank();
    }
}
