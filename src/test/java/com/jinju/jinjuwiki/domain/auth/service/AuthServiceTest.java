package com.jinju.jinjuwiki.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jinju.jinjuwiki.domain.auth.dto.request.LoginRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.SignupRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.LoginResponse;
import com.jinju.jinjuwiki.domain.auth.dto.response.SignupResponse;
import com.jinju.jinjuwiki.domain.auth.repository.EmailVerificationRepository;
import com.jinju.jinjuwiki.domain.user.entity.User;
import com.jinju.jinjuwiki.domain.user.entity.UserRole;
import com.jinju.jinjuwiki.domain.user.repository.UserRepository;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import com.jinju.jinjuwiki.global.security.JwtTokenProvider;
import com.jinju.jinjuwiki.global.security.UserPrincipal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

// 인증 서비스 단위 테스트 클래스
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private AuthValidationService authValidationService;

    @Mock
    private EmailSender emailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("회원가입에 성공하면 사용자 기본 정보를 반환한다.")
    void signupSuccess() {
        // given
        SignupRequest request = new SignupRequest("user1@test.com", "password123", "user1");
        User savedUser = User.builder()
                .email("user1@test.com")
                .password("encoded-password")
                .nickname("user1")
                .role(UserRole.USER)
                .build();
        ReflectionTestUtils.setField(savedUser, "id", 1L);

        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertThat(response.userId()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("user1@test.com");
        assertThat(response.nickname()).isEqualTo("user1");
        verify(authValidationService).validateDuplicateSignup("user1@test.com", "user1");
        verify(authValidationService).validateEmailVerified("user1@test.com");
        verify(passwordEncoder).encode("password123");
        verify(emailVerificationRepository).deleteByEmail("user1@test.com");
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입하면 예외가 발생한다.")
    void signupDuplicateEmail() {
        // given
        SignupRequest request = new SignupRequest("dup@test.com", "password123", "user2");
        doThrow(new BusinessException(ErrorCode.DUPLICATE_EMAIL))
                .when(authValidationService)
                .validateDuplicateSignup("dup@test.com", "user2");

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> authService.signup(request));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("로그인에 성공하면 사용자 정보를 반환한다.")
    void loginSuccess() {
        // given
        User user = User.builder()
                .email("login@test.com")
                .password("encoded-password")
                .nickname("loginUser")
                .role(UserRole.USER)
                .build();
        when(userRepository.findByEmail("login@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtTokenProvider.createAccessToken(any(UserPrincipal.class))).thenReturn("access-token");

        // when
        LoginResponse response = authService.login(new LoginRequest("login@test.com", "password123"));

        // then
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.email()).isEqualTo("login@test.com");
        assertThat(response.nickname()).isEqualTo("loginUser");
        assertThat(response.role()).isEqualTo("USER");
        verify(userRepository).findByEmail("login@test.com");
        verify(passwordEncoder).matches("password123", "encoded-password");
    }

    @Test
    @DisplayName("비밀번호가 다르면 로그인에 실패한다.")
    void loginFailWithInvalidPassword() {
        // given
        User user = User.builder()
                .email("wrong@test.com")
                .password("encoded-password")
                .nickname("wrongUser")
                .role(UserRole.USER)
                .build();
        when(userRepository.findByEmail("wrong@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad-password", "encoded-password")).thenReturn(false);

        // when
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> authService.login(new LoginRequest("wrong@test.com", "bad-password"))
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_LOGIN);
        verify(userRepository).findByEmail("wrong@test.com");
        verify(passwordEncoder).matches("bad-password", "encoded-password");
    }
}
