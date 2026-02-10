package com.souzip.api.domain.auth.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.souzip.api.domain.auth.dto.AppleUserInfo;
import com.souzip.api.domain.auth.dto.OAuthUserInfo;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
@Component
public class AppleApiClient implements OAuthClient {

    private final ObjectMapper objectMapper;

    @Override
    public OAuthUserInfo getUserInfo(String identityToken) {
        try {
            return parseIdentityToken(identityToken);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.APPLE_API_ERROR);
        }
    }

    private AppleUserInfo parseIdentityToken(String identityToken) {
        String[] parts = splitToken(identityToken);
        String payload = decodePayload(parts[1]);
        AppleUserInfo userInfo = parseJson(payload);

        log.info("Apple 로그인 - sub: {}, transferSub: {}",
            userInfo.getProviderId(), userInfo.getTransferSub());

        return userInfo;
    }

    private String[] splitToken(String identityToken) {
        String[] parts = identityToken.split("\\.");
        if (parts.length < 2) {
            throw new BusinessException(ErrorCode.INVALID_JWT_STRUCTURE);
        }
        return parts;
    }

    private String decodePayload(String encodedPayload) {
        try {
            return new String(Base64.getUrlDecoder().decode(encodedPayload));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.TOKEN_DECODE_FAILED);
        }
    }

    private AppleUserInfo parseJson(String payload) {
        try {
            return objectMapper.readValue(payload, AppleUserInfo.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TOKEN_PARSE_FAILED);
        }
    }
}
