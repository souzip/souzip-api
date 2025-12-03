package com.souzip.api.global.oauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@RequiredArgsConstructor
@Component
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

    private Kakao kakao;

    @Getter
    @RequiredArgsConstructor
    public static class Kakao {
        private final String userInfoUrl;
    }

}
