package com.souzip.application.email;

import com.souzip.application.email.dto.EmailBroadcastResult;
import com.souzip.application.email.provided.UserEmailFinder;
import com.souzip.global.config.MailSmtpProperties;
import com.souzip.global.exception.BusinessException;
import com.souzip.global.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailBroadcastService {

    private final UserEmailFinder userEmailFinder;
    private final ObjectProvider<JavaMailSender> javaMailSender;
    private final MailSmtpProperties mailSmtpProperties;

    // 지정한 한 주소로만 발송합니다(연동·문구 검증용).
    public EmailBroadcastResult sendTestToSingleAddress(String to, String subject, String body) {
        JavaMailSender sender = javaMailSender.getIfAvailable();
        if (sender == null || !mailSmtpProperties.isEnabled()) {
            log.warn("JavaMailSender 가 없거나 mail.smtp.enabled=false 입니다. 테스트 메일을 보내지 않습니다.");
            return new EmailBroadcastResult(1, 0, 0, false);
        }
        String from = resolveFromAddress();
        try {
            sendPlain(from, to, subject, body, sender);
            return new EmailBroadcastResult(1, 1, 0, true);
        } catch (MessagingException | MailException e) {
            log.warn("테스트 이메일 발송 실패 toMasked={} error={}", maskEmail(to), e.getMessage());
            return new EmailBroadcastResult(1, 0, 1, true);
        }
    }

    // SMTP가 설정된 경우 전체(탈퇴 제외) 회원의 고유 이메일로 동일 제목·본문을 발송합니다.
    public EmailBroadcastResult broadcastToAllMemberEmails(String subject, String body) {
        List<String> emails = userEmailFinder.findDistinctEmailsForActiveUsers();
        if (emails.isEmpty()) {
            return new EmailBroadcastResult(0, 0, 0, isSmtpReady());
        }
        JavaMailSender sender = javaMailSender.getIfAvailable();
        if (sender == null || !mailSmtpProperties.isEnabled()) {
            log.warn("JavaMailSender 가 없거나 mail.smtp.enabled=false 입니다. 이메일 발송을 건너뜁니다. 대상={}건", emails.size());
            return new EmailBroadcastResult(emails.size(), 0, 0, false);
        }
        String from = resolveFromAddress();
        int success = 0;
        int fail = 0;
        for (String to : emails) {
            try {
                sendPlain(from, to, subject, body, sender);
                success++;
            } catch (MessagingException | MailException e) {
                fail++;
                log.warn("이메일 발송 실패(다음 주소로 계속) toDomain={} error={}", maskEmail(to), e.getMessage());
            }
        }
        return new EmailBroadcastResult(emails.size(), success, fail, true);
    }

    private boolean isSmtpReady() {
        return javaMailSender.getIfAvailable() != null && mailSmtpProperties.isEnabled();
    }

    private void sendPlain(String from, String to, String subject, String body, JavaMailSender sender)
            throws MessagingException {
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, false);
        sender.send(message);
    }

    private String resolveFromAddress() {
        if (StringUtils.hasText(mailSmtpProperties.getFrom())) {
            return mailSmtpProperties.getFrom();
        }
        if (StringUtils.hasText(mailSmtpProperties.getUsername())) {
            return mailSmtpProperties.getUsername();
        }
        throw new BusinessException(ErrorCode.EMAIL_SEND_FAILED, "발신 주소(mail.smtp.from 또는 username)가 없습니다.");
    }

    private static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int at = email.indexOf('@');
        if (at <= 2) {
            return "***@" + email.substring(at + 1);
        }
        return email.substring(0, 2) + "***@" + email.substring(at + 1);
    }
}
