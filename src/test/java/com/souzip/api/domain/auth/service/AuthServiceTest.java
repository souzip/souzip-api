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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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
                return "kakao123";
            }

            @Override
            public String getName() {
                return "수집";
            }
        };
    }

    @Test
    @DisplayName("신규 사용자 로그인 시 User를 생성하고 토큰을 발급한다.")
    void login_newUser_createsUserAndIssuesTokens() {
        // given
        OAuthUserInfo oauthUserInfo = createOAuthUserInfo();
        User newUser = User.of(Provider.KAKAO, oauthUserInfo);
        User spyUser = spy(newUser);
        given(spyUser.getCreatedAt()).willReturn(LocalDateTime.now());
        given(spyUser.getUserId()).willReturn("550e8400");

        given(oauthClientFactory.getClient(Provider.KAKAO)).willReturn(oauthClient);
        given(oauthClient.getUserInfo("kakao_token")).willReturn(oauthUserInfo);
        given(userRepository.findByProviderAndProviderId(Provider.KAKAO, "kakao123"))
            .willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(spyUser);
        given(jwtTokenProvider.generateToken("550e8400")).willReturn("access_token");
        given(jwtTokenProvider.generateRefreshToken("550e8400")).willReturn("refresh_token");
        given(refreshTokenRepository.findByUser(any())).willReturn(Optional.empty());

        // when
        LoginResponse response = authService.login(Provider.KAKAO, "kakao_token");

        // then
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
        assertThat(response.getUser().nickname()).isEqualTo("수집");
        assertThat(response.isNewUser()).isTrue();

        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("기존 사용자 로그인 시 User를 조회하고 토큰을 발급한다.")
    void login_existingUser_findsUserAndIssuesTokens() {
        // given
        OAuthUserInfo oauthUserInfo = createOAuthUserInfo();
        User existingUser = User.of(Provider.KAKAO, oauthUserInfo);
        User spyUser = spy(existingUser);

        given(spyUser.getCreatedAt()).willReturn(LocalDateTime.now().minusDays(10));
        given(spyUser.getUserId()).willReturn("550e8400");

        RefreshToken existingToken = RefreshToken.of(
            spyUser,
            "old_refresh_token",
            LocalDateTime.now().plusDays(20)
        );

        given(oauthClientFactory.getClient(Provider.KAKAO)).willReturn(oauthClient);
        given(oauthClient.getUserInfo("kakao_token")).willReturn(oauthUserInfo);
        given(userRepository.findByProviderAndProviderId(Provider.KAKAO, "kakao123"))
            .willReturn(Optional.of(spyUser));
        given(jwtTokenProvider.generateToken("550e8400")).willReturn("access_token");
        given(jwtTokenProvider.generateRefreshToken("550e8400")).willReturn("refresh_token");
        given(refreshTokenRepository.findByUser(spyUser)).willReturn(Optional.of(existingToken));

        // when
        LoginResponse response = authService.login(Provider.KAKAO, "kakao_token");

        // then
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh_token");
        assertThat(response.isNewUser()).isFalse();

        verify(userRepository, never()).save(any(User.class));
        verify(spyUser, never()).restore(anyString(), anyString());
    }

    @Test
    @DisplayName("탈퇴 회원 재로그인 시 User를 복구하고 isNewUser는 true다.")
    void login_withdrawnUser_restoresUserAndIsNewUserTrue() {
        // given
        OAuthUserInfo oauthUserInfo = createOAuthUserInfo();
        User withdrawnUser = User.of(Provider.KAKAO, oauthUserInfo);
        User spyUser = spy(withdrawnUser);

        given(spyUser.getCreatedAt()).willReturn(LocalDateTime.now().minusDays(10));
        given(spyUser.isDeleted()).willReturn(true);
        given(spyUser.getUserId()).willReturn("550e8400");

        doCallRealMethod().when(spyUser).restore(anyString(), anyString());

        given(oauthClientFactory.getClient(Provider.KAKAO)).willReturn(oauthClient);
        given(oauthClient.getUserInfo("kakao_token")).willReturn(oauthUserInfo);
        given(userRepository.findByProviderAndProviderId(Provider.KAKAO, "kakao123"))
            .willReturn(Optional.of(spyUser));
        given(jwtTokenProvider.generateToken("550e8400")).willReturn("access_token");
        given(jwtTokenProvider.generateRefreshToken("550e8400")).willReturn("refresh_token");
        given(refreshTokenRepository.findByUser(any())).willReturn(Optional.empty());

        // when
        LoginResponse response = authService.login(Provider.KAKAO, "kakao_token");

        // then
        verify(spyUser).restore("수집", "수집");
        assertThat(response.isNewUser()).isTrue();
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Refresh Token이 없으면 새로 생성한다.")
    void login_withoutRefreshToken_createsNewToken() {
        // given
        OAuthUserInfo oauthUserInfo = createOAuthUserInfo();
        User newUser = User.of(Provider.KAKAO, oauthUserInfo);
        User spyUser = spy(newUser);

        given(spyUser.getCreatedAt()).willReturn(LocalDateTime.now());
        given(spyUser.getUserId()).willReturn("550e8400");

        given(oauthClientFactory.getClient(Provider.KAKAO)).willReturn(oauthClient);
        given(oauthClient.getUserInfo("kakao_token")).willReturn(oauthUserInfo);
        given(userRepository.findByProviderAndProviderId(Provider.KAKAO, "kakao123"))
            .willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(spyUser);
        given(jwtTokenProvider.generateToken(anyString())).willReturn("access_token");
        given(jwtTokenProvider.generateRefreshToken(anyString())).willReturn("refresh_token");
        given(refreshTokenRepository.findByUser(any())).willReturn(Optional.empty());

        // when
        authService.login(Provider.KAKAO, "kakao_token");

        // then
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Refresh Token이 있으면 업데이트한다.")
    void login_withExistingRefreshToken_updatesToken() {
        // given
        OAuthUserInfo oauthUserInfo = createOAuthUserInfo();
        User existingUser = User.of(Provider.KAKAO, oauthUserInfo);
        User spyUser = spy(existingUser);

        given(spyUser.getCreatedAt()).willReturn(LocalDateTime.now().minusDays(10));
        given(spyUser.getUserId()).willReturn("550e8400");

        RefreshToken existingToken = spy(RefreshToken.of(
            spyUser,
            "old_refresh_token",
            LocalDateTime.now().plusDays(20)
        ));

        given(oauthClientFactory.getClient(Provider.KAKAO)).willReturn(oauthClient);
        given(oauthClient.getUserInfo("kakao_token")).willReturn(oauthUserInfo);
        given(userRepository.findByProviderAndProviderId(Provider.KAKAO, "kakao123"))
            .willReturn(Optional.of(spyUser));
        given(jwtTokenProvider.generateToken(anyString())).willReturn("access_token");
        given(jwtTokenProvider.generateRefreshToken(anyString())).willReturn("new_refresh_token");
        given(refreshTokenRepository.findByUser(spyUser)).willReturn(Optional.of(existingToken));

        // when
        authService.login(Provider.KAKAO, "kakao_token");

        // then
        verify(existingToken).updateToken(eq("new_refresh_token"), any(LocalDateTime.class));
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Refresh Token이 유효하고 만료 임박하지 않으면 Access Token만 재발급한다.")
    void refresh_withValidToken_returnsNewAccessTokenOnly() {
        // given
        User user = User.of(Provider.KAKAO, createOAuthUserInfo());
        User spyUser = spy(user);
        given(spyUser.getUserId()).willReturn("550e8400");

        RefreshToken refreshToken = RefreshToken.of(
            spyUser,
            "valid_refresh_token",
            LocalDateTime.now().plusDays(20)
        );
        RefreshToken spyToken = spy(refreshToken);
        given(spyToken.isExpired()).willReturn(false);
        given(spyToken.getExpiresAt()).willReturn(LocalDateTime.now().plusDays(20));

        given(refreshTokenRepository.findByToken("valid_refresh_token"))
            .willReturn(Optional.of(spyToken));
        given(jwtTokenProvider.generateToken("550e8400"))
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
        User spyUser = spy(user);
        given(spyUser.getUserId()).willReturn("550e8400");

        RefreshToken refreshToken = RefreshToken.of(
            spyUser,
            "expiring_token",
            LocalDateTime.now().plusDays(5)
        );
        RefreshToken spyToken = spy(refreshToken);
        given(spyToken.isExpired()).willReturn(false);
        given(spyToken.getExpiresAt()).willReturn(LocalDateTime.now().plusDays(5));

        given(refreshTokenRepository.findByToken("expiring_token"))
            .willReturn(Optional.of(spyToken));
        given(jwtTokenProvider.generateToken("550e8400"))
            .willReturn("new_access_token");
        given(jwtTokenProvider.generateRefreshToken("550e8400"))
            .willReturn("new_refresh_token");

        // when
        RefreshResponse response = authService.refresh("expiring_token");

        // then
        assertThat(response.accessToken()).isEqualTo("new_access_token");
        assertThat(response.refreshToken()).isEqualTo("new_refresh_token");
        verify(spyToken).updateToken(eq("new_refresh_token"), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("만료된 Refresh Token으로 재발급 시 토큰을 삭제하고 에러가 발생한다.")
    void refresh_withExpiredToken_deletesTokenAndThrowsException() {
        // given
        User user = User.of(Provider.KAKAO, createOAuthUserInfo());

        RefreshToken expiredToken = RefreshToken.of(
            user,
            "expired_token",
            LocalDateTime.now().minusDays(1)
        );
        RefreshToken spyToken = spy(expiredToken);
        given(spyToken.isExpired()).willReturn(true);

        given(refreshTokenRepository.findByToken("expired_token"))
            .willReturn(Optional.of(spyToken));

        // when & then
        assertThatThrownBy(() -> authService.refresh("expired_token"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("만료된 Refresh Token입니다.");

        verify(refreshTokenRepository).delete(spyToken);
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

    @ParameterizedTest
    @DisplayName("Refresh Token 만료 임계값 경계 조건 테스트")
    @CsvSource({
        "11, false, 11일 남음 - 갱신 안 함",
        "10, true, 정확히 10일 남음 - 갱신",
        "9, true, 9일 남음 - 갱신"
    })
    void refresh_boundaryCondition_test(int daysRemaining, boolean shouldRenew, String description) {
        // given
        User user = User.of(Provider.KAKAO, createOAuthUserInfo());
        User spyUser = spy(user);
        given(spyUser.getUserId()).willReturn("550e8400");

        String tokenValue = "token_" + daysRemaining + "_days";
        RefreshToken refreshToken = RefreshToken.of(
            spyUser,
            tokenValue,
            LocalDateTime.now().plusDays(daysRemaining)
        );
        RefreshToken spyToken = spy(refreshToken);
        given(spyToken.isExpired()).willReturn(false);
        given(spyToken.getExpiresAt()).willReturn(LocalDateTime.now().plusDays(daysRemaining));

        given(refreshTokenRepository.findByToken(tokenValue))
            .willReturn(Optional.of(spyToken));
        given(jwtTokenProvider.generateToken("550e8400"))
            .willReturn("new_access_token");

        if (shouldRenew) {
            given(jwtTokenProvider.generateRefreshToken("550e8400"))
                .willReturn("new_refresh_token");
        }

        // when
        RefreshResponse response = authService.refresh(tokenValue);

        // then
        assertThat(response.accessToken()).isEqualTo("new_access_token");

        if (shouldRenew) {
            assertThat(response.refreshToken()).isEqualTo("new_refresh_token");
            verify(spyToken).updateToken(eq("new_refresh_token"), any(LocalDateTime.class));
        } else {
            assertThat(response.refreshToken()).isEqualTo(tokenValue);
            verify(jwtTokenProvider, never()).generateRefreshToken(anyString());
            verify(spyToken, never()).updateToken(anyString(), any(LocalDateTime.class));
        }
    }

    @Test
    @DisplayName("로그아웃 시 Refresh Token을 삭제한다.")
    void logout_success() {
        // given
        User user = User.of(Provider.KAKAO, createOAuthUserInfo());
        User spyUser = spy(user);

        RefreshToken refreshToken = RefreshToken.of(
            spyUser,
            "refresh_token",
            LocalDateTime.now().plusDays(30)
        );

        given(userRepository.findById(1L))
            .willReturn(Optional.of(spyUser));
        given(refreshTokenRepository.findByUser(spyUser))
            .willReturn(Optional.of(refreshToken));

        // when
        authService.logout(1L);

        // then
        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 로그아웃 시 에러가 발생한다.")
    void logout_withNotExistUser_throwsException() {
        // given
        given(userRepository.findById(999L))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.logout(999L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("사용자를 찾을 수 없습니다.");
    }
}
