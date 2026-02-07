package com.souzip.api.domain.migration.apple.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.souzip.api.global.config.AppleMigrationConfig;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleTransferService {

    private final AppleMigrationConfig appleConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String APPLE_MIGRATION_URL = "https://appleid.apple.com/auth/usermigrationinfo";
    private static final long JWT_EXPIRATION_MILLIS = 3600000L;

    public String getTransferIdentifier(String userId) {
        try {
            TransferResponse response = callAppleMigrationApi(userId);
            return extractTransferSub(response, userId);
        } catch (Exception e) {
            log.error("Apple transfer identifier 조회 실패 - userId: {}", userId, e);
            throw new BusinessException(ErrorCode.APPLE_MIGRATION_FAILED);
        }
    }

    private TransferResponse callAppleMigrationApi(String userId) {
        String clientSecret = generateClientSecret();
        HttpEntity<MultiValueMap<String, String>> request = buildRequest(userId, clientSecret);

        ResponseEntity<TransferResponse> response = restTemplate.postForEntity(
            APPLE_MIGRATION_URL,
            request,
            TransferResponse.class
        );

        return response.getBody();
    }

    private HttpEntity<MultiValueMap<String, String>> buildRequest(String userId, String clientSecret) {
        HttpHeaders headers = createHeaders();
        MultiValueMap<String, String> params = createParams(userId, clientSecret);
        return new HttpEntity<>(params, headers);
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private MultiValueMap<String, String> createParams(String userId, String clientSecret) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("sub", userId);
        params.add("target", appleConfig.getTeamId());
        params.add("client_id", appleConfig.getClientId());
        params.add("client_secret", clientSecret);
        return params;
    }

    private String extractTransferSub(TransferResponse response, String userId) {
        if (hasValidTransferSub(response)) {
            return logAndReturnTransferSub(response, userId);
        }

        logNoTransferSub(userId);
        return null;
    }

    private boolean hasValidTransferSub(TransferResponse response) {
        return response != null && response.getTransferSub() != null;
    }

    private String logAndReturnTransferSub(TransferResponse response, String userId) {
        String transferSub = response.getTransferSub();
        log.info("transfer_identifier 조회 성공 - userId: {}, transferId: {}", userId, transferSub);
        return transferSub;
    }

    private void logNoTransferSub(String userId) {
        log.warn("transfer_identifier 없음 - userId: {}", userId);
    }

    private String generateClientSecret() {
        try {
            PrivateKey privateKey = getPrivateKey();
            String jwt = buildJwt(privateKey);
            logJwtGeneration(jwt);
            return jwt;
        } catch (Exception e) {
            log.error("client_secret 생성 실패", e);
            throw new BusinessException(ErrorCode.APPLE_MIGRATION_FAILED);
        }
    }

    private String buildJwt(PrivateKey privateKey) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expiryDate = new Date(nowMillis + JWT_EXPIRATION_MILLIS);

        return Jwts.builder()
            .header()
            .add("kid", appleConfig.getKeyId())
            .add("alg", "ES256")
            .and()
            .issuer(appleConfig.getTeamId())
            .issuedAt(now)
            .expiration(expiryDate)
            .audience()
            .add("https://appleid.apple.com")
            .and()
            .subject(appleConfig.getClientId())
            .signWith(privateKey, Jwts.SIG.ES256)
            .compact();
    }

    private void logJwtGeneration(String jwt) {
        log.info("Generated JWT: {}", jwt);
        log.info("Team ID: {}", appleConfig.getTeamId());
        log.info("Key ID: {}", appleConfig.getKeyId());
        log.info("Client ID: {}", appleConfig.getClientId());
    }

    private PrivateKey getPrivateKey() {
        try {
            String cleanedKey = cleanPrivateKey(appleConfig.getPrivateKey());
            byte[] keyBytes = Base64.getDecoder().decode(cleanedKey);
            return generatePrivateKey(keyBytes);
        } catch (Exception e) {
            log.error("Private Key 파싱 실패", e);
            throw new BusinessException(ErrorCode.APPLE_MIGRATION_FAILED);
        }
    }

    private String cleanPrivateKey(String privateKey) {
        return privateKey
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\n", "")
            .replaceAll("\\s+", "");
    }

    private PrivateKey generatePrivateKey(byte[] keyBytes) throws Exception {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePrivate(keySpec);
    }

    @Getter
    public static class TransferResponse {
        @JsonProperty("transfer_sub")
        private String transferSub;
    }
}
