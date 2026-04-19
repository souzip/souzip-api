package com.souzip.auth.adapter.social;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.souzip.auth.application.dto.SocialUserInfo;
import com.souzip.auth.application.exception.AuthException;
import com.souzip.auth.application.required.SocialClient;
import com.souzip.domain.shared.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
@Component
public class AppleSocialClient implements SocialClient {

    private final ObjectMapper objectMapper;

    @Override
    public Provider getProvider() {
        return Provider.APPLE;
    }

    @Override
    public SocialUserInfo getUserInfo(String identityToken) {
        try {
            String[] parts = identityToken.split("\\.");
            if (parts.length < 2) throw AuthException.invalidJwtStructure();

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            AppleTokenPayload token = objectMapper.readValue(payload, AppleTokenPayload.class);

            return new SocialUserInfo(token.sub(), token.email());
        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            log.error("애플 사용자 정보 조회 중 오류 발생", e);
            throw AuthException.appleApiError();
        }
    }

    private record AppleTokenPayload(String sub, String email) {
    }
}