package com.souzip.api.domain.auth.service;

import com.souzip.api.domain.auth.client.OAuthClient;
import com.souzip.api.domain.auth.client.OAuthClientFactory;
import com.souzip.api.domain.auth.dto.LoginResponse;
import com.souzip.api.domain.auth.dto.LoginUserInfo;
import com.souzip.api.domain.auth.dto.OAuthUserInfo;
import com.souzip.api.domain.auth.dto.RefreshResponse;
import com.souzip.api.domain.auth.entity.RefreshToken;
import com.souzip.api.domain.auth.repository.RefreshTokenRepository;
import com.souzip.api.domain.user.entity.Provider;
import com.souzip.api.domain.user.entity.User;
import com.souzip.api.domain.user.repository.UserRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import com.souzip.api.global.security.jwt.JwtProperties;
import com.souzip.api.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private static final int REFRESH_TOKEN_VALIDITY_DAYS = 30;
    private static final int REFRESH_TOKEN_RENEWAL_THRESHOLD_DAYS = 10;

    private final OAuthClientFactory oauthClientFactory;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Transactional
    public LoginResponse login(Provider provider, String oauthAccessToken) {
        OAuthClient oauthClient = oauthClientFactory.getClient(provider);
        OAuthUserInfo oauthUserInfo = oauthClient.getUserInfo(oauthAccessToken);

        User user = findOrCreateUser(provider, oauthUserInfo);
        boolean isNewUser = isNewUser(user);

        String accessToken = jwtTokenProvider.generateToken(user.getUserId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        saveRefreshToken(user, refreshToken);

        LoginUserInfo userInfo = LoginUserInfo.from(user);
        return LoginResponse.of(accessToken, refreshToken, userInfo, isNewUser);
    }

    @Transactional
    public RefreshResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = findRefreshToken(refreshTokenValue);
        validateRefreshToken(refreshToken);

        User user = refreshToken.getUser();

        String newAccessToken = jwtTokenProvider.generateToken(user.getUserId());

        if (isRefreshTokenExpiringSoon(refreshToken)) {
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());
            updateRefreshToken(refreshToken, newRefreshToken);
            log.info("Refresh Token 갱신: userId={}, remainingDays={}",
                user.getUserId(), getRemainingDays(refreshToken));
            return RefreshResponse.of(newAccessToken, newRefreshToken);
        }

        return RefreshResponse.of(newAccessToken, refreshToken.getToken());
    }

    private User findOrCreateUser(Provider provider, OAuthUserInfo oauthUserInfo) {
        return userRepository
            .findByProviderAndProviderId(provider, oauthUserInfo.getProviderId())
            .orElseGet(() -> createUser(provider, oauthUserInfo));
    }

    private User createUser(Provider provider, OAuthUserInfo oauthUserInfo) {
        User user = User.of(provider, oauthUserInfo);
        return userRepository.save(user);
    }

    private boolean isNewUser(User user) {
        return user.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(5));
    }

    private void saveRefreshToken(User user, String tokenValue) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(jwtProperties.getRefreshExpiration());

        refreshTokenRepository.findByUser(user)
            .ifPresentOrElse(
                token -> updateExistingToken(token, tokenValue, expiresAt),
                () -> createNewToken(user, tokenValue, expiresAt)
            );
    }

    private void updateExistingToken(RefreshToken token, String tokenValue, LocalDateTime expiresAt) {
        token.updateToken(tokenValue, expiresAt);
    }

    private void createNewToken(User user, String tokenValue, LocalDateTime expiresAt) {
        RefreshToken newToken = RefreshToken.of(user, tokenValue, expiresAt);
        refreshTokenRepository.save(newToken);
    }

    private RefreshToken findRefreshToken(String tokenValue) {
        return refreshTokenRepository.findByToken(tokenValue)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
    }

    private void validateRefreshToken(RefreshToken refreshToken) {
        if (refreshToken.isExpired()) {
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }
    }

    private boolean isRefreshTokenExpiringSoon(RefreshToken refreshToken) {
        LocalDateTime threshold = LocalDateTime.now().plusDays(REFRESH_TOKEN_RENEWAL_THRESHOLD_DAYS);
        return refreshToken.getExpiresAt().isBefore(threshold);
    }

    private void updateRefreshToken(RefreshToken refreshToken, String newTokenValue) {
        LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS);
        refreshToken.updateToken(newTokenValue, newExpiresAt);
    }

    private long getRemainingDays(RefreshToken refreshToken) {
        return java.time.Duration.between(LocalDateTime.now(), refreshToken.getExpiresAt()).toDays();
    }
}
