package com.souzip.auth.adapter.social;

import com.souzip.auth.application.dto.SocialUserInfo;
import com.souzip.auth.application.exception.AuthException;
import com.souzip.auth.application.required.SocialClient;
import com.souzip.shared.domain.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Component
public class GoogleSocialClient implements SocialClient {

    private final RestTemplate restTemplate;
    private final SocialProperties socialProperties;

    @Override
    public Provider getProvider() {
        return Provider.GOOGLE;
    }

    @Override
    public SocialUserInfo getUserInfo(String accessToken) {
        try {
            GoogleUserResponse response = restTemplate.exchange(
                    socialProperties.getGoogle().getUserInfoUrl(),
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders(accessToken)),
                    GoogleUserResponse.class
            ).getBody();

            return new SocialUserInfo(response.id(), response.email());
        } catch (Exception e) {
            log.error("구글 사용자 정보 조회 중 오류 발생", e);
            throw AuthException.googleApiError();
        }
    }

    private HttpHeaders createAuthHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }

    private record GoogleUserResponse(String id, String email) {
    }
}