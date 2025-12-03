package com.souzip.api.global.oauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

    private final Kakao kakao;

    @Getter
    @RequiredArgsConstructor
    public static class Kakao {
        private final String userInfoUrl;
    }

}
