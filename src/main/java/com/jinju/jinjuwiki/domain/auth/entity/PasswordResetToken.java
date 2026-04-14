package com.jinju.jinjuwiki.domain.auth.entity;

import com.jinju.jinjuwiki.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 비밀번호 재설정 토큰 엔티티
@Getter
@Entity
@Table(name = "password_reset_tokens")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordResetToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(length = 100)
    private String resetToken;

    @Column
    private LocalDateTime verifiedAt;

    @Column
    private LocalDateTime resetTokenExpiresAt;

    @Builder
    private PasswordResetToken(String email, String token, LocalDateTime expiresAt,
                               String resetToken, LocalDateTime verifiedAt, LocalDateTime resetTokenExpiresAt) {
        this.email = email;
        this.token = token;
        this.expiresAt = expiresAt;
        this.resetToken = resetToken;
        this.verifiedAt = verifiedAt;
        this.resetTokenExpiresAt = resetTokenExpiresAt;
    }

    // 비밀번호 재설정 인증코드 재발급 메서드
    public void reissue(String token, LocalDateTime expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.resetToken = null;
        this.verifiedAt = null;
        this.resetTokenExpiresAt = null;
    }

    // 비밀번호 재설정 확인 완료 기록 메서드
    public void verify(String resetToken, LocalDateTime verifiedAt, LocalDateTime resetTokenExpiresAt) {
        this.resetToken = resetToken;
        this.verifiedAt = verifiedAt;
        this.resetTokenExpiresAt = resetTokenExpiresAt;
    }
}
