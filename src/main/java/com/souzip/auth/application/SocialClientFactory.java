package com.souzip.auth.application;

import com.souzip.auth.application.exception.AuthException;
import com.souzip.auth.application.required.SocialClient;
import com.souzip.domain.shared.Provider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SocialClientFactory {

    private final Map<Provider, SocialClient> clients;

    public SocialClientFactory(List<SocialClient> socialClients) {
        this.clients = socialClients.stream()
                .collect(Collectors.toMap(SocialClient::getProvider, c -> c));
    }

    public SocialClient getClient(Provider provider) {
        return Optional.ofNullable(clients.get(provider))
                .orElseThrow(AuthException::unsupportedProvider);
    }
}