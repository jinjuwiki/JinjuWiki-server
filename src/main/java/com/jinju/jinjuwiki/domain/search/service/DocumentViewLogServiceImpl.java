package com.jinju.jinjuwiki.domain.search.service;

import com.jinju.jinjuwiki.domain.document.entity.Document;
import com.jinju.jinjuwiki.domain.search.entity.DocumentViewLog;
import com.jinju.jinjuwiki.domain.search.repository.DocumentViewLogRepository;
import com.jinju.jinjuwiki.domain.user.entity.User;
import com.jinju.jinjuwiki.domain.user.repository.UserRepository;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 문서 조회 로그 저장 서비스 구현체
@Service
@RequiredArgsConstructor
public class DocumentViewLogServiceImpl implements DocumentViewLogService {

    private final DocumentViewLogRepository documentViewLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void save(Document document, Long viewerUserId, String viewerIp) {
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
}
