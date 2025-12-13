package com.souzip.api.domain.auth.client;

import com.souzip.api.domain.auth.dto.GoogleUserInfo;
import com.souzip.api.domain.auth.dto.OAuthUserInfo;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import com.souzip.api.global.oauth.OAuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Component
public class GoogleApiClient implements OAuthClient {

    private final RestTemplate restTemplate;
    private final OAuthProperties oAuthProperties;

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        try {
            return callGoogleUserInfoApi(accessToken);
        } catch (Exception e) {
            log.error("구글 사용자 정보 조회 중 오류 발생", e);
            throw new BusinessException(ErrorCode.GOOGLE_API_ERROR);
        }
    }

    private GoogleUserInfo callGoogleUserInfoApi(String accessToken) {
        String url = oAuthProperties.getGoogle().getUserInfoUrl();
        HttpHeaders headers = createAuthHeaders(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            request,
            GoogleUserInfo.class
        );

        return response.getBody();
    }

    private HttpHeaders createAuthHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }
}
