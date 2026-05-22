package com.souzip.global.config;

import com.souzip.global.oauth.OAuthProperties;
import com.souzip.global.security.jwt.JwtProperties;
import com.souzip.adapter.config.ObjectStorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    JwtProperties.class,
    OAuthProperties.class,
    ObjectStorageProperties.class,
    ClovaStudioProperties.class,
    AppleMigrationConfig.class,
    CorsProperties.class
})
public class AppConfig {
}
