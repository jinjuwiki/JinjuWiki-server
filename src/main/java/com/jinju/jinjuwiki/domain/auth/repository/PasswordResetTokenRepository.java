package com.jinju.jinjuwiki.domain.auth.repository;

import com.jinju.jinjuwiki.domain.auth.entity.PasswordResetToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// 비밀번호 재설정 토큰 저장소 인터페이스
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    // 이메일 기준 재설정 토큰 조회 메서드
    Optional<PasswordResetToken> findByEmail(String email);
}
