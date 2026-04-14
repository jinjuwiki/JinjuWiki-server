package com.jinju.jinjuwiki.domain.auth.service;

import com.jinju.jinjuwiki.domain.auth.dto.request.PasswordResetRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.PasswordResetVerifyRequest;
import com.jinju.jinjuwiki.domain.auth.dto.request.LoginRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.LoginResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationSendRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.EmailVerificationSendResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationVerifyRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.EmailVerificationVerifyResponse;
import com.jinju.jinjuwiki.domain.auth.dto.response.PasswordResetVerifyResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.SignupRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.SignupResponse;
import com.jinju.jinjuwiki.domain.auth.entity.EmailVerification;
import com.jinju.jinjuwiki.domain.auth.entity.PasswordResetToken;
import com.jinju.jinjuwiki.domain.auth.repository.EmailVerificationRepository;
import com.jinju.jinjuwiki.domain.auth.repository.PasswordResetTokenRepository;
import com.jinju.jinjuwiki.domain.user.entity.User;
import com.jinju.jinjuwiki.domain.user.entity.UserRole;
import com.jinju.jinjuwiki.domain.user.repository.UserRepository;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import com.jinju.jinjuwiki.global.security.JwtTokenProvider;
import com.jinju.jinjuwiki.global.security.UserPrincipal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 인증 로직 처리 서비스
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // Authorization 헤더에서 사용하는 JWT 토큰 값
    private static final String TOKEN_TYPE_BEARER = "Bearer";

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AuthValidationService authValidationService;
    private final RedisEmailVerificationSendRateLimiter redisEmailVerificationSendRateLimiter;
    private final RedisPasswordResetRequestRateLimiter redisPasswordResetRequestRateLimiter;
    private final RedisEmailVerificationVerifyAttemptLimiter redisEmailVerificationVerifyAttemptLimiter;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.auth.email-verification-expiration-minutes}")
    private long emailVerificationExpirationMinutes;

    @Value("${app.auth.password-reset-expiration-minutes:30}")
    private long passwordResetExpirationMinutes;

    @Override
    @Transactional
    // 이메일 인증코드 발송 로직
    public EmailVerificationSendResponse sendVerificationCode(EmailVerificationSendRequest request) {
        // 이메일 인증 발송 요청 제한 확인 호출
        validateEmailVerificationSendAllowed(request.email());
        authValidationService.validateEmailAvailable(request.email());

        String code = generateVerificationCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(emailVerificationExpirationMinutes);

        Optional<EmailVerification> existing = emailVerificationRepository.findByEmail(request.email());
        if (existing.isPresent()) {
            existing.get().reissue(code, expiresAt);
        } else {
            emailVerificationRepository.save(EmailVerification.builder()
                    .email(request.email())
                    .code(code)
                    .verified(false)
                    .expiresAt(expiresAt)
                    .build());
        }

        emailSender.sendVerificationCode(request.email(), code);
        return new EmailVerificationSendResponse(request.email(), expiresAt);
    }

    @Override
    @Transactional
    // 이메일 인증코드 검증 로직
    public EmailVerificationVerifyResponse verifyCode(EmailVerificationVerifyRequest request) {
        // 이메일 인증코드 검증 시도 제한 확인 호출
        validateEmailVerificationVerifyAllowed(request.email());

        EmailVerification verification = emailVerificationRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        if (verification.isExpired(now)) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
        }

        if (!verification.matches(request.code())) {
            // 이메일 인증코드 검증 실패 누적 메서드
            long failureCount = redisEmailVerificationVerifyAttemptLimiter.recordFailure(request.email());
            if (failureCount >= 5L) {
                throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_ATTEMPT_EXCEEDED);
            }
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        verification.verify(now);
        // 이메일 인증코드 검증 실패 기록 초기화 메서드
        redisEmailVerificationVerifyAttemptLimiter.reset(request.email());
        return new EmailVerificationVerifyResponse(verification.getEmail(), true, verification.getVerifiedAt());
    }

    @Override
    @Transactional
    // 회원가입 로직
    public SignupResponse signup(SignupRequest request) {
        authValidationService.validateDuplicateSignup(request.email(), request.nickname());
        authValidationService.validateEmailVerified(request.email());

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password())) // 비밀번호 암호화
                .nickname(request.nickname())
                .role(UserRole.USER)
                .build();

        User savedUser = userRepository.save(user); // DB 저장
        emailVerificationRepository.deleteByEmail(request.email());

        return new SignupResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getNickname());
    }

    @Override
    // 로그인 로직
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN));

        // 평문 비교 X
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN); // 에러 통일
        }

        String accessToken = jwtTokenProvider.createAccessToken(UserPrincipal.from(user));

        return new LoginResponse(
                accessToken,
                TOKEN_TYPE_BEARER,
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name()
        );
    }

    @Override
    @Transactional
    // 비밀번호 재설정 요청 로직
    public void requestPasswordReset(PasswordResetRequest request) {
        // 비밀번호 재설정 요청 제한 확인 호출
        validatePasswordResetRequestAllowed(request.email());

        // 사용자 열거 방지용 존재 여부 은닉 메서드
        Optional<User> user = userRepository.findByEmail(request.email());
        if (user.isEmpty()) {
            return;
        }

        String code = generatePasswordResetCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(passwordResetExpirationMinutes);

        Optional<PasswordResetToken> existing = passwordResetTokenRepository.findByEmail(request.email());
        if (existing.isPresent()) {
            existing.get().reissue(code, expiresAt);
        } else {
            passwordResetTokenRepository.save(PasswordResetToken.builder()
                    .email(request.email())
                    .token(code)
                    .expiresAt(expiresAt)
                    .build());
        }

        emailSender.sendPasswordResetCode(request.email(), code);
    }

    @Override
    @Transactional
    // 비밀번호 재설정 인증코드 확인 로직
    public PasswordResetVerifyResponse verifyPasswordResetCode(PasswordResetVerifyRequest request) {
        throw new UnsupportedOperationException("Password reset verify not implemented yet");
    }

    // 이메일 인증코드 생성 메서드
    private String generateVerificationCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
    }

    // 비밀번호 재설정 인증코드 생성 메서드
    private String generatePasswordResetCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
    }

    // 이메일 인증 발송 제한 예외 변환 메서드
    private void validateEmailVerificationSendAllowed(String email) {
        try {
            redisEmailVerificationSendRateLimiter.validateAllowed(email);
        } catch (BusinessException exception) {
            if (exception.getErrorCode() == ErrorCode.INVALID_INPUT) {
                throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_SEND_RATE_LIMITED);
            }
            throw exception;
        }
    }

    // 비밀번호 재설정 요청 제한 예외 변환 메서드
    private void validatePasswordResetRequestAllowed(String email) {
        try {
            redisPasswordResetRequestRateLimiter.validateAllowed(email);
        } catch (BusinessException exception) {
            if (exception.getErrorCode() == ErrorCode.INVALID_INPUT) {
                throw new BusinessException(ErrorCode.PASSWORD_RESET_REQUEST_RATE_LIMITED);
            }
            throw exception;
        }
    }

    // 이메일 인증코드 검증 제한 예외 변환 메서드
    private void validateEmailVerificationVerifyAllowed(String email) {
        try {
            redisEmailVerificationVerifyAttemptLimiter.validateAllowed(email);
        } catch (BusinessException exception) {
            if (exception.getErrorCode() == ErrorCode.INVALID_INPUT) {
                throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_ATTEMPT_EXCEEDED);
            }
            throw exception;
        }
    }
}
