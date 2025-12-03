package com.souzip.api.domain.auth.client;

import com.souzip.api.domain.user.entity.Provider;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

@Component
public class OAuthClientFactory {

    private final Map<Provider, OAuthClient> clients;

    public OAuthClientFactory(
        KakaoApiClient kakaoApiClient
    ) {
        this.clients = new EnumMap<>(Provider.class);
        clients.put(Provider.KAKAO, kakaoApiClient);
    }

    public OAuthClient getClient(Provider provider) {
        OAuthClient client = clients.get(provider);

        if (isUnsupportedProvider(client)) {
            throw createUnsupportedProviderException(provider);
        }

        return client;
    }

    private static boolean isUnsupportedProvider(OAuthClient client) {
        return client == null;
    }

    private BusinessException createUnsupportedProviderException(Provider provider) {
        return new BusinessException(
            ErrorCode.INVALID_INPUT,
            provider.name() + " 로그인은 아직 지원하지 않습니다."
        );
    }
}
