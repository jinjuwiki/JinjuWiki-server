package com.jinju.jinjuwiki.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jinju.jinjuwiki.domain.user.dto.request.UserNicknameUpdateRequest;
import com.jinju.jinjuwiki.domain.user.dto.request.UserPasswordUpdateRequest;
import com.jinju.jinjuwiki.domain.user.dto.response.UserNicknameUpdateResponse;
import com.jinju.jinjuwiki.domain.user.entity.User;
import com.jinju.jinjuwiki.domain.user.entity.UserRole;
import com.jinju.jinjuwiki.domain.user.repository.UserRepository;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

// 사용자 서비스 테스트 클래스
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("사용자는 자신의 닉네임을 변경할 수 있다.")
    void updateNicknameSuccess() {
        // given
        User user = createUser(1L, "user@test.com", "oldNick", "encoded-password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname("newNick")).thenReturn(false);

        // when
        UserNicknameUpdateResponse response = userService.updateNickname(1L, new UserNicknameUpdateRequest("newNick"));

        // then
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.nickname()).isEqualTo("newNick");
        assertThat(user.getNickname()).isEqualTo("newNick");
        verify(userRepository).existsByNickname("newNick");
    }

    @Test
    @DisplayName("이미 사용 중인 닉네임으로 변경하면 예외가 발생한다.")
    void updateNicknameFailWhenDuplicate() {
        // given
        User user = createUser(1L, "user@test.com", "oldNick", "encoded-password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname("newNick")).thenReturn(true);

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updateNickname(1L, new UserNicknameUpdateRequest("newNick"))
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_NICKNAME);
    }

    @Test
    @DisplayName("사용자는 현재 비밀번호 확인 후 새 비밀번호로 변경할 수 있다.")
    void updatePasswordSuccess() {
        // given
        User user = createUser(1L, "user@test.com", "nick", "encoded-password");
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("old-password", "new-password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "encoded-password")).thenReturn(true);
        when(passwordEncoder.matches("new-password", "encoded-password")).thenReturn(false);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");

        // when
        userService.updatePassword(1L, request);

        // then
        assertThat(user.getPassword()).isEqualTo("encoded-new-password");
        verify(passwordEncoder).encode("new-password");
    }

    @Test
    @DisplayName("현재 비밀번호가 다르면 변경할 수 없다.")
    void updatePasswordFailWhenCurrentPasswordMismatch() {
        // given
        User user = createUser(1L, "user@test.com", "nick", "encoded-password");
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("wrong-password", "new-password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updatePassword(1L, request)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_PASSWORD);
        verify(passwordEncoder, never()).encode("new-password");
    }

    @Test
    @DisplayName("새 비밀번호가 현재 비밀번호와 같으면 변경할 수 없다.")
    void updatePasswordFailWhenSameAsOldPassword() {
        // given
        User user = createUser(1L, "user@test.com", "nick", "encoded-password");
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("old-password", "old-password");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "encoded-password")).thenReturn(true);
        when(passwordEncoder.matches("old-password", "encoded-password")).thenReturn(true);

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updatePassword(1L, request)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SAME_AS_OLD_PASSWORD);
        verify(passwordEncoder, never()).encode("old-password");
    }

    // 테스트용 사용자 생성 함수
    private User createUser(Long id, String email, String nickname, String password) {
        User user = User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
