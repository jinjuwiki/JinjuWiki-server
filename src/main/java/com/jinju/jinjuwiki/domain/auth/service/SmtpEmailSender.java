package com.jinju.jinjuwiki.domain.auth.service;

import com.jinju.jinjuwiki.global.error.BusinessException;
import com.jinju.jinjuwiki.global.error.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@jinjuwiki.local}")
    private String from;

    @Override
    public void sendVerificationCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("[JinjuWiki] 이메일 인증번호 안내");
            helper.setText(buildVerificationEmailHtml(code), true);

            mailSender.send(message);
        } catch (MailException | MessagingException ex) {
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    public void sendPasswordResetLink(String to, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("[JinjuWiki] 비밀번호 재설정 안내");
            helper.setText(buildPasswordResetEmailHtml(token), true);

            mailSender.send(message);
        } catch (MailException | MessagingException ex) {
            throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    // 인증 메일을 카드형 레이아웃으로 구성해 Gmail에서도 읽기 쉽게 만듦
    private String buildVerificationEmailHtml(String code) {
        String escapedCode = escapeHtml(code);

        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>JinjuWiki 이메일 인증번호</title>
                </head>
                <body style="margin:0; padding:0; background-color:#f4f7fb; font-family:'Apple SD Gothic Neo','Malgun Gothic',sans-serif; color:#1f2937;">
                    <div style="width:100%%; background-color:#f4f7fb; padding:40px 16px;">
                        <div style="max-width:600px; margin:0 auto; background-color:#ffffff; border-radius:24px; overflow:hidden; box-shadow:0 18px 48px rgba(15, 23, 42, 0.08);">
                            <div style="background:linear-gradient(135deg, #00A0E9 0%%, #0284c7 100%%); padding:32px 40px; text-align:center;">
                                <div style="display:inline-block; padding:8px 14px; border-radius:999px; background-color:rgba(255,255,255,0.14); color:#eff6ff; font-size:13px; font-weight:700; letter-spacing:0.08em; text-transform:uppercase;">
                                    JinjuWiki
                                </div>
                                <h1 style="margin:18px 0 0; color:#ffffff; font-size:30px; line-height:1.35; font-weight:600;">
                                    이메일 인증번호 안내
                                </h1>
                            </div>

                            <div style="padding:40px;">
                                <p style="margin:0 0 12px; font-size:16px; line-height:1.7;">
                                    안녕하세요. JinjuWiki입니다.
                                </p>
                                <p style="margin:0 0 28px; font-size:16px; line-height:1.7; color:#475569;">
                                    회원가입을 완료하려면 아래 인증번호를 입력해주세요.
                                </p>

                                <div style="margin:0 0 28px; padding:24px; border-radius:20px; background-color:#eff6ff; border:1px solid #bfdbfe; text-align:center;">
                                    <p style="margin:0 0 12px; font-size:13px; line-height:1.5; letter-spacing:0.08em; text-transform:uppercase; color:#1d4ed8; font-weight:700;">
                                        Verification Code
                                    </p>
                                    <div style="font-size:40px; line-height:1.1; font-weight:800; letter-spacing:0.18em; color:#0f172a;">
                                        %s
                                    </div>
                                </div>

                                <div style="margin:0 0 28px; padding:20px 22px; border-radius:16px; background-color:#f8fafc; border:1px solid #e2e8f0;">
                                    <p style="margin:0; font-size:15px; line-height:1.7; color:#334155;">
                                        인증번호는 <strong style="color:#0f172a;">5분</strong> 동안만 유효합니다.<br>
                                        본인이 요청하지 않았다면 이 메일은 무시하셔도 됩니다.
                                    </p>
                                </div>

                                <p style="margin:0; font-size:14px; line-height:1.7; color:#64748b;">
                                    문의가 필요하면 JinjuWiki 운영팀에 연락해주세요.
                                </p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(escapedCode);
    }

    // 재설정 토큰 메일 본문 구성
    private String buildPasswordResetEmailHtml(String token) {
        String escapedToken = escapeHtml(token);

        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>JinjuWiki 비밀번호 재설정</title>
                </head>
                <body style="margin:0; padding:0; background-color:#f4f7fb; font-family:'Apple SD Gothic Neo','Malgun Gothic',sans-serif; color:#1f2937;">
                    <div style="width:100%%; background-color:#f4f7fb; padding:40px 16px;">
                        <div style="max-width:600px; margin:0 auto; background-color:#ffffff; border-radius:24px; overflow:hidden; box-shadow:0 18px 48px rgba(15, 23, 42, 0.08);">
                            <div style="background:linear-gradient(135deg, #00A0E9 0%%, #0284c7 100%%); padding:32px 40px; text-align:center;">
                                <div style="display:inline-block; padding:8px 14px; border-radius:999px; background-color:rgba(255,255,255,0.14); color:#eff6ff; font-size:13px; font-weight:700; letter-spacing:0.08em; text-transform:uppercase;">
                                    JinjuWiki
                                </div>
                                <h1 style="margin:18px 0 0; color:#ffffff; font-size:30px; line-height:1.35; font-weight:600;">
                                    비밀번호 재설정 안내
                                </h1>
                            </div>

                            <div style="padding:40px;">
                                <p style="margin:0 0 12px; font-size:16px; line-height:1.7;">
                                    비밀번호 재설정을 위한 요청이 접수되었습니다.
                                </p>
                                <p style="margin:0 0 28px; font-size:16px; line-height:1.7; color:#475569;">
                                    아래 토큰을 이용해 비밀번호 재설정을 진행해주세요.
                                </p>

                                <div style="margin:0 0 28px; padding:24px; border-radius:20px; background-color:#eff6ff; border:1px solid #bfdbfe; text-align:center; word-break:break-all;">
                                    <p style="margin:0 0 12px; font-size:13px; line-height:1.5; letter-spacing:0.08em; text-transform:uppercase; color:#1d4ed8; font-weight:700;">
                                        Reset Token
                                    </p>
                                    <div style="font-size:18px; line-height:1.6; font-weight:700; color:#0f172a;">
                                        %s
                                    </div>
                                </div>

                                <div style="margin:0 0 28px; padding:20px 22px; border-radius:16px; background-color:#f8fafc; border:1px solid #e2e8f0;">
                                    <p style="margin:0; font-size:15px; line-height:1.7; color:#334155;">
                                        토큰은 일정 시간 이후 만료됩니다.<br>
                                        본인이 요청하지 않았다면 이 메일은 무시하셔도 됩니다.
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(escapedToken);
    }

    // 사용자 입력이 HTML에 그대로 들어갈 때 깨지지 않도록 최소한의 이스케이프를 적용
    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
