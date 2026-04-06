package com.jinju.jinjuwiki.domain.auth.service;

public interface EmailSender {

    void sendVerificationCode(String to, String code);
}
