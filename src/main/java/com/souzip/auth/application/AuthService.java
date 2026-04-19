package com.souzip.auth.application;

import com.souzip.auth.application.dto.LoginInfo;
import com.souzip.auth.application.dto.RefreshInfo;
import com.souzip.auth.application.dto.UserInfo;
import com.souzip.auth.application.exception.AuthException;
import com.souzip.auth.application.provided.Auth;
import com.souzip.auth.application.required.RefreshTokenRepository;
import com.souzip.auth.application.required.TokenProvider;
import com.souzip.auth.application.required.UserPort;
import com.souzip.auth.domain.RefreshToken;
import com.souzip.shared.domain.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService implements Auth {

    private final SocialClientFactory socialClientFactory;
    private final UserPort userPort;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;

    @Transactional
    @Override
    public LoginInfo login(Provider provider, String accessToken) {
        var socialUserInfo = socialClientFactory.getClient(provider)
                .getUserInfo(accessToken);

        UserInfo userInfo = userPort.findOrCreateUser(
                provider,
                socialUserInfo.providerId(),
                socialUserInfo.email()
        );

        refreshTokenRepository.deleteByUserId(userInfo.id());

        String newRefreshToken = tokenProvider.generateRefreshToken();
        refreshTokenRepository.save(
                RefreshToken.create(userInfo.id(), newRefreshToken, tokenProvider.getRefreshTokenExpiresAt())
        );

        log.info("로그인 성공 - userId: {}, provider: {}", userInfo.id(), provider);

        return new LoginInfo(
                tokenProvider.generateAccessToken(userInfo.id()),
                newRefreshToken,
                userInfo.needsOnboarding(),
                userInfo.userId(),
                userInfo.nickname()
        );
    }

    @Transactional
    @Override
    public RefreshInfo refresh(String refreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(AuthException::invalidRefreshToken);

        if (storedToken.isExpired()) {
            refreshTokenRepository.delete(storedToken);

            throw AuthException.expiredRefreshToken();
        }

        String newRefreshToken = tokenProvider.generateRefreshToken();

        storedToken.rotate(newRefreshToken, tokenProvider.getRefreshTokenExpiresAt());

        refreshTokenRepository.save(storedToken);

        log.debug("토큰 재발급 - userId: {}", storedToken.getUserId());

        return new RefreshInfo(
                tokenProvider.generateAccessToken(storedToken.getUserId()),
                newRefreshToken
        );
    }

    @Transactional
    @Override
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);

        log.info("로그아웃 - userId: {}", userId);
    }
}