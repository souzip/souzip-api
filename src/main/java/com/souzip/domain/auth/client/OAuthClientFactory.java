package com.souzip.domain.auth.client;

import com.souzip.domain.user.entity.Provider;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class OAuthClientFactory {

    private final Map<Provider, OAuthClient> clients;

    public OAuthClientFactory(
        KakaoApiClient kakaoApiClient,
        GoogleApiClient googleApiClient,
        AppleApiClient appleApiClient
    ) {
        this.clients = new EnumMap<>(Provider.class);
        clients.put(Provider.KAKAO, kakaoApiClient);
        clients.put(Provider.GOOGLE, googleApiClient);
        clients.put(Provider.APPLE, appleApiClient);
    }
    public OAuthClient getClient(Provider provider) {
        return clients.get(provider);
    }
}
