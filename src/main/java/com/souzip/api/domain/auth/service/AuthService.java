package com.souzip.api.domain.auth.service;

import com.souzip.api.domain.audit.entity.AuditAction;
import com.souzip.api.domain.auth.dto.AppleUserInfo;
import com.souzip.api.global.audit.annotation.Audit;
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
import com.souzip.api.global.security.jwt.JwtTokenProvider;
import java.util.Optional;
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

    @Audit(action = AuditAction.LOGIN)
    @Transactional
    public LoginResponse login(Provider provider, String oauthAccessToken) {
        OAuthClient oauthClient = oauthClientFactory.getClient(provider);
        OAuthUserInfo oauthUserInfo = oauthClient.getUserInfo(oauthAccessToken);

        User user = findOrCreateUser(provider, oauthUserInfo);

        user.updateLastLoginAt();

        String accessToken = jwtTokenProvider.generateToken(user.getUserId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());

        saveRefreshToken(user, refreshToken);

        LoginUserInfo userInfo = LoginUserInfo.from(user);
        boolean needsOnboarding = user.needsOnboarding();

        return LoginResponse.of(accessToken, refreshToken, userInfo, needsOnboarding);
    }

    @Transactional
    public RefreshResponse refresh(String refreshTokenValue) {
        RefreshToken refreshToken = findRefreshToken(refreshTokenValue);
        validateRefreshToken(refreshToken);

        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateToken(user.getUserId());

        if (isRefreshTokenExpiringSoon(refreshToken)) {
            return renewRefreshToken(refreshToken, user, newAccessToken);
        }

        return RefreshResponse.of(newAccessToken, refreshToken.getToken());
    }

    @Audit(action = AuditAction.LOGOUT, userIdParam = "currentUserId")
    @Transactional
    public void logout(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        refreshTokenRepository.findByUser(user)
            .ifPresent(refreshTokenRepository::delete);
    }

    private User findOrCreateUser(Provider provider, OAuthUserInfo oauthUserInfo) {
        if (isAppleProvider(provider)) {
            return findOrCreateAppleUser(oauthUserInfo);
        }

        return findOrCreateGeneralUser(provider, oauthUserInfo);
    }

    private User findOrCreateGeneralUser(Provider provider, OAuthUserInfo oauthUserInfo) {
        Optional<User> existingUser = userRepository
            .findByProviderAndProviderId(provider, oauthUserInfo.getProviderId());

        return existingUser.map(user -> restoreIfDeleted(user, oauthUserInfo))
            .orElseGet(() -> createUser(provider, oauthUserInfo));
    }

    private User findOrCreateAppleUser(OAuthUserInfo oauthUserInfo) {
        String newProviderId = oauthUserInfo.getProviderId();
        String transferSub = extractTransferSub(oauthUserInfo);

        Optional<User> existingUser = findByProviderId(newProviderId);
        if (existingUser.isPresent()) {
            return handleExistingUser(existingUser.get(), oauthUserInfo, newProviderId);
        }

        if (hasTransferSub(transferSub)) {
            Optional<User> migratedUser = attemptMigration(transferSub, newProviderId, oauthUserInfo);

            return migratedUser.orElseGet(() -> createNewAppleUser(newProviderId, oauthUserInfo));
        }

        return createNewAppleUser(newProviderId, oauthUserInfo);
    }

    private Optional<User> findByProviderId(String providerId) {
        return userRepository.findByProviderAndProviderId(Provider.APPLE, providerId);
    }

    private String extractTransferSub(OAuthUserInfo oauthUserInfo) {
        if (oauthUserInfo instanceof AppleUserInfo appleUserInfo) {
            return appleUserInfo.getTransferSub();
        }
        return null;
    }

    private boolean hasTransferSub(String transferSub) {
        return transferSub != null;
    }

    private User handleExistingUser(User user, OAuthUserInfo oauthUserInfo, String providerId) {
        log.info("Apple 사용자 로그인 - providerId로 찾음: {}", providerId);
        return restoreIfDeleted(user, oauthUserInfo);
    }

    private Optional<User> attemptMigration(String transferSub, String newProviderId, OAuthUserInfo oauthUserInfo) {
        Optional<User> existingUser = userRepository.findByTransferIdentifier(transferSub);

        return existingUser.map(user -> migrateUser(user, transferSub, newProviderId, oauthUserInfo));
    }

    private User migrateUser(User user, String transferSub, String newProviderId, OAuthUserInfo oauthUserInfo) {
        logMigrationDetected(user.getProviderId(), transferSub, newProviderId);

        user.updateProviderId(newProviderId);
        userRepository.save(user);

        User restoredUser = restoreIfDeleted(user, oauthUserInfo);

        log.info("Apple 사용자 마이그레이션 완료: userId={}", user.getId());
        return restoredUser;
    }

    private void logMigrationDetected(String oldProviderId, String transferSub, String newProviderId) {
        log.info("Apple 앱 이전 감지 - transfer_sub로 기존 사용자 발견");
        log.info("기존 providerId: {}, transfer_sub: {}, 새 providerId: {}",
            oldProviderId, transferSub, newProviderId);
    }

    private User createNewAppleUser(String providerId, OAuthUserInfo oauthUserInfo) {
        log.info("새로운 Apple 사용자 생성: providerId={}", providerId);
        return createUser(Provider.APPLE, oauthUserInfo);
    }

    private User restoreIfDeleted(User user, OAuthUserInfo oauthUserInfo) {
        if (user.isDeleted()) {
            String name = oauthUserInfo.getName();
            user.restore(name);
        }
        return user;
    }

    private User createUser(Provider provider, OAuthUserInfo oauthUserInfo) {
        User user = User.of(provider, oauthUserInfo);
        return userRepository.save(user);
    }

    private boolean isAppleProvider(Provider provider) {
        return provider == Provider.APPLE;
    }

    private RefreshResponse renewRefreshToken(RefreshToken refreshToken, User user, String newAccessToken) {
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getUserId());
        updateRefreshToken(refreshToken, newRefreshToken);

        log.info("Refresh Token 갱신: userId={}, remainingDays={}",
            user.getUserId(), getRemainingDays(refreshToken));

        return RefreshResponse.of(newAccessToken, newRefreshToken);
    }

    private void saveRefreshToken(User user, String tokenValue) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(REFRESH_TOKEN_VALIDITY_DAYS);

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
            deleteExpiredToken(refreshToken);
            throw new BusinessException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }
    }

    private void deleteExpiredToken(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
        log.info("만료된 Refresh Token 삭제: token={}", refreshToken.getId());
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
