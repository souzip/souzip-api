package com.souzip.global.config;

import java.util.Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.StringUtils;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "mail.smtp.enabled", havingValue = "true")
public class MailSmtpConfig {

    private final MailSmtpProperties mailSmtpProperties;

    @Bean
    public JavaMailSender javaMailSender() {
        if (!StringUtils.hasText(mailSmtpProperties.getUsername())
                || !StringUtils.hasText(mailSmtpProperties.getPassword())) {
            throw new IllegalStateException(
                    "mail.smtp.enabled=true 일 때 mail.smtp.username 과 mail.smtp.password 가 필요합니다."
            );
        }
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mailSmtpProperties.getHost());
        sender.setPort(mailSmtpProperties.getPort());
        sender.setUsername(mailSmtpProperties.getUsername());
        sender.setPassword(mailSmtpProperties.getPassword());

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "false");
        return sender;
    }
}
