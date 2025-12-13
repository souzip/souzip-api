package com.souzip.api.global.oauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

    private final Kakao kakao;
    private final Google google;

    @Getter
    @RequiredArgsConstructor
    public static class Kakao {
        private final String userInfoUrl;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Google {
        private final String userInfoUrl;
    }
}
