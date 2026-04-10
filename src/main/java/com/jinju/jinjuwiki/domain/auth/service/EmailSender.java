package com.jinju.jinjuwiki.domain.auth.service;

// 인증 메일 발송 인터페이스
public interface EmailSender {

    // 이메일 인증코드 메일 발송 메서드
    void sendVerificationCode(String to, String code);

    // 비밀번호 재설정 메일 발송 메서드
    void sendPasswordResetLink(String to, String token);
}
