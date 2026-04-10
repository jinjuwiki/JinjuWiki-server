package com.jinju.jinjuwiki.domain.search.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jinju.jinjuwiki.domain.category.entity.Category;
import com.jinju.jinjuwiki.domain.document.entity.Document;
import com.jinju.jinjuwiki.domain.search.entity.DocumentViewLog;
import com.jinju.jinjuwiki.domain.search.repository.DocumentViewLogRepository;
import com.jinju.jinjuwiki.domain.user.entity.User;
import com.jinju.jinjuwiki.domain.user.entity.UserRole;
import com.jinju.jinjuwiki.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

// 문서 조회 로그 서비스 단위 테스트 클래스
@ExtendWith(MockitoExtension.class)
class DocumentViewLogServiceTest {

    @Mock
    private DocumentViewLogRepository documentViewLogRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DocumentViewLogServiceImpl documentViewLogService;

    @Test
    @DisplayName("로그인 사용자 조회 로그를 저장할 수 있다.")
    void saveUserViewLogSuccess() {
        // given
        Document document = createDocument(1L, "급상승 문서");
        User user = createUser(2L, "viewer@test.com", "viewer");
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(documentViewLogRepository.countByDocumentIdAndUserIdAndCreatedAtAfter(eq(1L), eq(2L), any()))
                .thenReturn(0L);

        // when
        documentViewLogService.save(document, 2L, null);

        // then
        verify(documentViewLogRepository).save(any(DocumentViewLog.class));
    }

    @Test
    @DisplayName("같은 사용자의 문서 조회는 최근 1시간 동안 최대 3회만 반영한다.")
    void saveUserViewLogSkipWhenUserLimitExceeded() {
        // given
        Document document = createDocument(1L, "급상승 문서");
        when(documentViewLogRepository.countByDocumentIdAndUserIdAndCreatedAtAfter(eq(1L), eq(2L), any()))
                .thenReturn(3L);

        // when
        documentViewLogService.save(document, 2L, null);

        // then
        verify(documentViewLogRepository, never()).save(any(DocumentViewLog.class));
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("같은 IP의 문서 조회는 최근 1시간 동안 최대 3회만 반영한다.")
    void saveAnonymousViewLogSkipWhenIpLimitExceeded() {
        // given
        Document document = createDocument(1L, "급상승 문서");
        when(documentViewLogRepository.countByDocumentIdAndViewerIpAndCreatedAtAfter(eq(1L), eq("127.0.0.1"), any()))
                .thenReturn(3L);

        // when
        documentViewLogService.save(document, null, "127.0.0.1");

        // then
        verify(documentViewLogRepository, never()).save(any(DocumentViewLog.class));
    }

    @Test
    @DisplayName("제한을 넘지 않은 IP 조회는 로그를 저장할 수 있다.")
    void saveAnonymousViewLogSuccess() {
        // given
        Document document = createDocument(1L, "급상승 문서");
        when(documentViewLogRepository.countByDocumentIdAndViewerIpAndCreatedAtAfter(eq(1L), eq("127.0.0.1"), any()))
                .thenReturn(2L);

        // when
        documentViewLogService.save(document, null, "127.0.0.1");

        // then
        verify(documentViewLogRepository).save(any(DocumentViewLog.class));
    }

    // 테스트용 사용자 생성 함수
    private User createUser(Long id, String email, String nickname) {
        User user = User.builder()
                .email(email)
                .password("encoded-password")
                .nickname(nickname)
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    // 테스트용 문서 생성 함수
    private Document createDocument(Long id, String title) {
        Category category = Category.builder()
                .name("학교")
                .build();
        ReflectionTestUtils.setField(category, "id", 1L);

        Document document = Document.builder()
                .title(title)
                .content("{}")
                .summary("요약")
                .eventYear(2024)
                .contentJson("{}")
                .author(createUser(3L, "author@test.com", "author"))
                .category(category)
                .build();
        ReflectionTestUtils.setField(document, "id", id);
        return document;
    }
}
