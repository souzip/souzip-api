package com.souzip.api.domain.auth.service;

import com.souzip.api.domain.auth.client.OAuthClient;
import com.souzip.api.domain.auth.client.OAuthClientFactory;
import com.souzip.api.domain.auth.dto.LoginResponse;
import com.souzip.api.domain.auth.dto.OAuthUserInfo;
import com.souzip.api.domain.auth.entity.RefreshToken;
import com.souzip.api.domain.auth.repository.RefreshTokenRepository;
import com.souzip.api.domain.user.entity.Provider;
import com.souzip.api.domain.user.entity.User;
import com.souzip.api.domain.user.repository.UserRepository;
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

    private OAuthUserInfo createOAuthUserInfo(String providerId, String name) {
        return new OAuthUserInfo() {
            @Override
            public String getProviderId() {
                return providerId;
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    // ===== login() 테스트만! =====

    @Test
    @DisplayName("카카오 로그인 시 신규 사용자를 생성하고 토큰을 발급한다.")
    void login_createsNewUser_whenUserNotExists() {
        // given
        String kakaoAccessToken = "kakao_token";
        OAuthUserInfo oauthUserInfo = createOAuthUserInfo("20251204", "수집");
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
        OAuthUserInfo oauthUserInfo = createOAuthUserInfo("20251204", "수집");
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
        OAuthUserInfo oauthUserInfo = createOAuthUserInfo("20251204", "수집");
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
        OAuthUserInfo oauthUserInfo = createOAuthUserInfo("20251204", "수집");
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
}
