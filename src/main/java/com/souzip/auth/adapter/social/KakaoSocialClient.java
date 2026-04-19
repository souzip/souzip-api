package com.souzip.auth.adapter.social;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.souzip.auth.application.dto.SocialUserInfo;
import com.souzip.auth.application.exception.AuthException;
import com.souzip.auth.application.required.SocialClient;
import com.souzip.domain.shared.Provider;
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
public class KakaoSocialClient implements SocialClient {

    private final RestTemplate restTemplate;
    private final SocialProperties socialProperties;

    @Override
    public Provider getProvider() {
        return Provider.KAKAO;
    }

    @Override
    public SocialUserInfo getUserInfo(String accessToken) {
        try {
            KakaoUserResponse response = restTemplate.exchange(
                    socialProperties.getKakao().getUserInfoUrl(),
                    HttpMethod.GET,
                    new HttpEntity<>(createAuthHeaders(accessToken)),
                    KakaoUserResponse.class
            ).getBody();

            return new SocialUserInfo(
                    String.valueOf(response.id()),
                    response.kakaoAccount().email()
            );
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 중 오류 발생", e);
            throw AuthException.kakaoApiError();
        }
    }

    private HttpHeaders createAuthHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }

    private record KakaoUserResponse(Long id, @JsonProperty("kakao_account") KakaoAccount kakaoAccount) {
    }

    private record KakaoAccount(String email) {
    }
}