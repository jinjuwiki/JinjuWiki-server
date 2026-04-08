package com.jinju.jinjuwiki.domain.auth.service;

import com.jinju.jinjuwiki.domain.auth.dto.request.LoginRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.LoginResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationSendRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.EmailVerificationSendResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.EmailVerificationVerifyRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.EmailVerificationVerifyResponse;
import com.jinju.jinjuwiki.domain.auth.dto.request.SignupRequest;
import com.jinju.jinjuwiki.domain.auth.dto.response.SignupResponse;
import com.jinju.jinjuwiki.domain.auth.entity.EmailVerification;
import com.jinju.jinjuwiki.domain.auth.repository.EmailVerificationRepository;
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

// 로그인/회원가입 로직 처리 클래스
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    // Authorization 헤더에서 사용하는 JWT 토큰 값
    private static final String TOKEN_TYPE_BEARER = "Bearer";

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.auth.email-verification-expiration-minutes}")
    private long emailVerificationExpirationMinutes;

    @Override
    @Transactional
    public EmailVerificationSendResponse sendVerificationCode(EmailVerificationSendRequest request) {
        validateEmailAvailable(request.email());

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
    public EmailVerificationVerifyResponse verifyCode(EmailVerificationVerifyRequest request) {
        EmailVerification verification = emailVerificationRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        if (verification.isExpired(now)) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
        }

        if (!verification.matches(request.code())) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        verification.verify(now);
        return new EmailVerificationVerifyResponse(verification.getEmail(), true, verification.getVerifiedAt());
    }

    @Override
    @Transactional
    public SignupResponse signup(SignupRequest request) {
        validateDuplicate(request);
        validateEmailVerified(request.email());

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

    // 중복 확인, 동시성 문제 발견( DB unique 혹은 다른 방법 모색해서 보완 필요 )
    private void validateDuplicate(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    private void validateEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    private void validateEmailVerified(String email) {
        EmailVerification verification = emailVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED));

        if (verification.isExpired(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
        }

        if (!verification.isVerified()) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
    }

    private String generateVerificationCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
    }
}
