package com.souzip.api.global.config;

import com.souzip.api.global.oauth.OAuthProperties;
import com.souzip.api.global.security.jwt.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    JwtProperties.class,
    OAuthProperties.class
})
public class AppConfig {
}
