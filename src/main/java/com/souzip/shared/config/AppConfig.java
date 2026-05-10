package com.souzip.shared.config;

import com.souzip.adapter.config.ObjectStorageProperties;
import com.souzip.auth.adapter.security.jwt.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        JwtProperties.class,
        ObjectStorageProperties.class,
        ClovaStudioProperties.class,
        AppleMigrationConfig.class,
        CorsProperties.class,
        MailSmtpProperties.class
})
public class AppConfig {
}
