package com.souzip.api.domain.auth.service;

import com.souzip.api.domain.auth.client.OAuthClient;
import com.souzip.api.domain.auth.client.OAuthClientFactory;
import com.souzip.api.domain.auth.dto.LoginResponse;
import com.souzip.api.domain.auth.dto.OAuthUserInfo;
import com.souzip.api.domain.auth.dto.RefreshResponse;
import com.souzip.api.domain.auth.entity.RefreshToken;
import com.souzip.api.domain.auth.repository.RefreshTokenRepository;
import com.souzip.api.domain.user.entity.Provider;
import com.souzip.api.domain.user.entity.User;
import com.souzip.api.domain.user.repository.UserRepository;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private OAuthClientFactory oauthClientFactory;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private OAuthClient oauthClient;

    @InjectMocks
    private AuthService authService;

    private OAuthUserInfo createOAuthUserInfo() {
        return new OAuthUserInfo() {
            @Override
            public String getProviderId() {
                return "20251204";
            }

            @Override
            public String getName() {
                return "수집";
            }
        };
    }

    @Test
    @DisplayName("카카오 로그인 시 신규 사용자를 생성하고 토큰을 발급한다.")
    void login_createsNewUser_whenUserNotExists() {
        // given
        String kakaoAccessToken = "kakao_token";
        OAuthUserInfo oauthUserInfo = createOAuthUserInfo();
        User newUser = User.of(Provider.KAKAO, oauthUserInfo);
        newUser = spy(newUser);

        given(newUser.getCreatedAt()).willReturn(LocalDateTime.now());
        given(oauthClientFactory.getClient(Provider.KAKAO)).willReturn(oauthClient);
        given(oauthClient.getUserInfo(kakaoAccessToken)).willReturn(oauthUserInfo);
        given(userRepository.findByProviderAndProviderId(Provider.KAKAO, "20251204"))
            .willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(newUser);
        given(jwtTokenProvider.generateToken(anyString())).willReturn("access_token");
        given(jwtTokenProvider.generateRefreshToken(anyString())).willReturn("refresh_token");
        given(refreshTokenRepository.findByUser(any(User.class))).willReturn(Optional.empty());

        // when
        LoginResponse response = authService.login(Provider.KAKAO, kakaoAccessToken);

        // then
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
        assertThat(response.getUser().nickname()).isEqualTo("수집");
        assertThat(response.isNewUser()).isTrue();

        then(userRepository).should().save(any(User.class));
        then(refreshTokenRepository).should().save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("카카오 로그인 시 기존 사용자를 조회하고 토큰을 발급한다.")
    void login_findsExistingUser_whenUserExists() {
        // given
        String kakaoAccessToken = "kakao_token";
        OAuthUserInfo oauthUserInfo = createOAuthUserInfo();
        User existingUser = User.of(Provider.KAKAO, oauthUserInfo);

        existingUser = spy(existingUser);
        given(existingUser.getCreatedAt()).willReturn(LocalDateTime.now().minusMinutes(10));

        RefreshToken existingToken = RefreshToken.of(
            existingUser,
            "old_refresh_token",
            LocalDateTime.now().plusDays(7)
        );

        given(oauthClientFactory.getClient(Provider.KAKAO)).willReturn(oauthClient);
        given(oauthClient.getUserInfo(kakaoAccessToken)).willReturn(oauthUserInfo);
        given(userRepository.findByProviderAndProviderId(Provider.KAKAO, "20251204"))
            .willReturn(Optional.of(existingUser));
        given(jwtTokenProvider.generateToken(anyString())).willReturn("access_token");
        given(jwtTokenProvider.generateRefreshToken(anyString())).willReturn("refresh_token");
        given(refreshTokenRepository.findByUser(existingUser)).willReturn(Optional.of(existingToken));

        // when
        LoginResponse response = authService.login(Provider.KAKAO, kakaoAccessToken);

        // then
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
        assertThat(response.isNewUser()).isFalse();

        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("RefreshToken이 없으면 새로 생성한다.")
    void login_createsRefreshToken_whenNotExists() {
        // given
        String kakaoAccessToken = "kakao_token";
        OAuthUserInfo oauthUserInfo = createOAuthUserInfo();
        User newUser = User.of(Provider.KAKAO, oauthUserInfo);
        newUser = spy(newUser);

        given(newUser.getCreatedAt()).willReturn(LocalDateTime.now());
        given(oauthClientFactory.getClient(Provider.KAKAO)).willReturn(oauthClient);
        given(oauthClient.getUserInfo(kakaoAccessToken)).willReturn(oauthUserInfo);
        given(userRepository.findByProviderAndProviderId(Provider.KAKAO, "20251204"))
            .willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(newUser);
        given(jwtTokenProvider.generateToken(anyString())).willReturn("access_token");
        given(jwtTokenProvider.generateRefreshToken(anyString())).willReturn("refresh_token");
        given(refreshTokenRepository.findByUser(any(User.class))).willReturn(Optional.empty());

        // when
        authService.login(Provider.KAKAO, kakaoAccessToken);

        // then
        then(refreshTokenRepository).should().save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("RefreshToken이 있으면 업데이트한다.")
    void login_updatesRefreshToken_whenExists() {
        // given
        String kakaoAccessToken = "kakao_token";
        OAuthUserInfo oauthUserInfo = createOAuthUserInfo();
        User existingUser = User.of(Provider.KAKAO, oauthUserInfo);
        existingUser = spy(existingUser);
        given(existingUser.getCreatedAt()).willReturn(LocalDateTime.now().minusMinutes(10));

        RefreshToken existingToken = spy(RefreshToken.of(
            existingUser,
            "old_refresh_token",
            LocalDateTime.now().plusDays(7)
        ));

        given(oauthClientFactory.getClient(Provider.KAKAO)).willReturn(oauthClient);
        given(oauthClient.getUserInfo(kakaoAccessToken)).willReturn(oauthUserInfo);
        given(userRepository.findByProviderAndProviderId(Provider.KAKAO, "20251204"))
            .willReturn(Optional.of(existingUser));
        given(jwtTokenProvider.generateToken(anyString())).willReturn("access_token");
        given(jwtTokenProvider.generateRefreshToken(anyString())).willReturn("new_refresh_token");
        given(refreshTokenRepository.findByUser(existingUser)).willReturn(Optional.of(existingToken));

        // when
        authService.login(Provider.KAKAO, kakaoAccessToken);

        // then
        then(existingToken).should().updateToken(eq("new_refresh_token"), any(LocalDateTime.class));
        then(refreshTokenRepository).should(never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Refresh Token이 유효하고 여유가 있으면 Access Token만 재발급한다.")
    void refresh_withValidToken_returnsNewAccessTokenOnly() {
        // given
        User user = User.of(Provider.KAKAO, createOAuthUserInfo());
        User savedUser = spy(user);
        given(savedUser.getUserId()).willReturn("550e8400-e29b-41d4-a716-446655440000");

        RefreshToken refreshToken = RefreshToken.of(
            savedUser,
            "valid_refresh_token",
            LocalDateTime.now().plusDays(20)
        );
        RefreshToken spyRefreshToken = spy(refreshToken);
        given(spyRefreshToken.isExpired()).willReturn(false);
        given(spyRefreshToken.getExpiresAt()).willReturn(LocalDateTime.now().plusDays(20));

        given(refreshTokenRepository.findByToken("valid_refresh_token"))
            .willReturn(Optional.of(spyRefreshToken));
        given(jwtTokenProvider.generateToken(anyString()))
            .willReturn("new_access_token");

        // when
        RefreshResponse response = authService.refresh("valid_refresh_token");

        // then
        assertThat(response.accessToken()).isEqualTo("new_access_token");
        assertThat(response.refreshToken()).isEqualTo("valid_refresh_token");
        verify(jwtTokenProvider, never()).generateRefreshToken(anyString());
    }

    @Test
    @DisplayName("Refresh Token이 10일 이하로 남으면 둘 다 재발급한다.")
    void refresh_withExpiringSoon_returnsBothNewTokens() {
        // given
        User user = User.of(Provider.KAKAO, createOAuthUserInfo());
        User savedUser = spy(user);
        given(savedUser.getUserId()).willReturn("550e8400-e29b-41d4-a716-446655440000");

        RefreshToken refreshToken = RefreshToken.of(
            savedUser,
            "expiring_soon_token",
            LocalDateTime.now().plusDays(5)
        );
        RefreshToken spyRefreshToken = spy(refreshToken);
        given(spyRefreshToken.isExpired()).willReturn(false);
        given(spyRefreshToken.getExpiresAt()).willReturn(LocalDateTime.now().plusDays(5));

        given(refreshTokenRepository.findByToken("expiring_soon_token"))
            .willReturn(Optional.of(spyRefreshToken));
        given(jwtTokenProvider.generateToken(anyString()))
            .willReturn("new_access_token");
        given(jwtTokenProvider.generateRefreshToken(anyString()))
            .willReturn("new_refresh_token");

        // when
        RefreshResponse response = authService.refresh("expiring_soon_token");

        // then
        assertThat(response.accessToken()).isEqualTo("new_access_token");
        assertThat(response.refreshToken()).isEqualTo("new_refresh_token");
        verify(spyRefreshToken).updateToken(eq("new_refresh_token"), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("만료된 Refresh Token으로 재발급 시 에러가 발생한다.")
    void refresh_withExpiredToken_throwsException() {
        // given
        User user = User.of(Provider.KAKAO, createOAuthUserInfo());

        RefreshToken expiredToken = RefreshToken.of(
            user,
            "expired_refresh_token",
            LocalDateTime.now().minusDays(1)
        );
        RefreshToken spyExpiredToken = spy(expiredToken);
        given(spyExpiredToken.isExpired()).willReturn(true);

        given(refreshTokenRepository.findByToken("expired_refresh_token"))
            .willReturn(Optional.of(spyExpiredToken));

        // when & then
        assertThatThrownBy(() -> authService.refresh("expired_refresh_token"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("만료된 Refresh Token입니다.");
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token으로 재발급 시 에러가 발생한다.")
    void refresh_withInvalidToken_throwsException() {
        // given
        given(refreshTokenRepository.findByToken("invalid_token"))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.refresh("invalid_token"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("유효하지 않은 Refresh Token입니다.");
    }
}
