package com.souzip.api.domain.auth.client;

import com.souzip.api.domain.auth.dto.KakaoUserInfo;
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
@Component
@RequiredArgsConstructor
public class KakaoApiClient implements OAuthClient {

    private final RestTemplate restTemplate;
    private final OAuthProperties oAuthProperties;

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        try {
            return callKakaoUserInfoApi(accessToken);
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 중 오류 발생", e);
            throw new BusinessException(ErrorCode.KAKAO_API_ERROR);
        }
    }

    private KakaoUserInfo callKakaoUserInfoApi(String accessToken) {
        String url = oAuthProperties.getKakao().getUserInfoUrl();
        HttpHeaders headers = createAuthHeaders(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            request,
            KakaoUserInfo.class
        );

        return response.getBody();
    }

    private HttpHeaders createAuthHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }
}
