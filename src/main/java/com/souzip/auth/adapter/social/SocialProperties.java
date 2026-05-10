package com.souzip.auth.adapter.social;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth")
public record SocialProperties(
        Kakao kakao,
        Google google
) {
    public record Kakao(String userInfoUrl) {
    }

    public record Google(String userInfoUrl) {
    }
}