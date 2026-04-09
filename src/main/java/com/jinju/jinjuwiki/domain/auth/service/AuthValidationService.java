package com.jinju.jinjuwiki.domain.auth.service;

import com.jinju.jinjuwiki.domain.auth.entity.EmailVerification;
import com.jinju.jinjuwiki.domain.auth.repository.EmailVerificationRepository;
import com.jinju.jinjuwiki.domain.user.repository.UserRepository;
import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// 인증 관련 사전 검증 전담 서비스
@Service
@RequiredArgsConstructor
public class AuthValidationService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;

    // 회원가입 중복 검증 호출
    public void validateDuplicateSignup(String email, String nickname) {
        validateEmailAvailable(email);

        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    // 이메일 중복 검증 호출
    public void validateEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }

    // 이메일 인증 완료 검증 호출
    public void validateEmailVerified(String email) {
        EmailVerification verification = emailVerificationRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED));

        if (!verification.isVerified()) {
            if (verification.isExpired(LocalDateTime.now())) {
                throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
            }
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
    }
}
