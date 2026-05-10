package com.souzip.auth.service;

import com.souzip.auth.application.AuthService;
import com.souzip.auth.application.SocialClientFactory;
import com.souzip.auth.application.dto.LoginInfo;
import com.souzip.auth.application.dto.RefreshInfo;
import com.souzip.auth.application.dto.SocialUserInfo;
import com.souzip.auth.application.dto.UserInfo;
import com.souzip.auth.application.exception.AuthException;
import com.souzip.auth.application.required.RefreshTokenRepository;
import com.souzip.auth.application.required.SocialClient;
import com.souzip.auth.application.required.TokenProvider;
import com.souzip.auth.application.required.UserPort;
import com.souzip.auth.domain.RefreshToken;
import com.souzip.shared.domain.Provider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SocialClientFactory socialClientFactory;

    @Mock
    private UserPort userPort;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private SocialClient socialClient;

    @InjectMocks
    private AuthService authService;

    private static final Long USER_ID = 1L;
    private static final String USER_STRING_ID = "550e8400";
    private static final String NICKNAME = "수집";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    private SocialUserInfo createSocialUserInfo() {
        return new SocialUserInfo("kakao123", "test@gmail.com");
    }

    private UserInfo createUserInfo(boolean needsOnboarding) {
        return new UserInfo(USER_ID, USER_STRING_ID, NICKNAME, needsOnboarding);
    }

    @Test
    @DisplayName("신규 사용자 로그인 시 토큰을 발급한다.")
    void login_newUser_issuesTokens() {
        // given
        given(socialClientFactory.getClient(Provider.KAKAO)).willReturn(socialClient);
        given(socialClient.getUserInfo("kakao_token")).willReturn(createSocialUserInfo());
        given(userPort.findOrCreateUser(eq(Provider.KAKAO), anyString(), anyString()))
                .willReturn(createUserInfo(true));
        given(tokenProvider.generateAccessToken(USER_ID)).willReturn(ACCESS_TOKEN);
        given(tokenProvider.generateRefreshToken()).willReturn(REFRESH_TOKEN);
        given(tokenProvider.getRefreshTokenExpiresAt()).willReturn(LocalDateTime.now().plusDays(30));

        // when
        LoginInfo result = authService.login(Provider.KAKAO, "kakao_token");

        // then
        assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(result.refreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(result.needsOnboarding()).isTrue();
        assertThat(result.userId()).isEqualTo(USER_STRING_ID);
        assertThat(result.nickname()).isEqualTo(NICKNAME);

        verify(refreshTokenRepository).deleteByUserId(USER_ID);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("기존 사용자 로그인 시 needsOnboarding은 false다.")
    void login_existingUser_needsOnboardingFalse() {
        // given
        given(socialClientFactory.getClient(Provider.KAKAO)).willReturn(socialClient);
        given(socialClient.getUserInfo("kakao_token")).willReturn(createSocialUserInfo());
        given(userPort.findOrCreateUser(eq(Provider.KAKAO), anyString(), anyString()))
                .willReturn(createUserInfo(false));
        given(tokenProvider.generateAccessToken(USER_ID)).willReturn(ACCESS_TOKEN);
        given(tokenProvider.generateRefreshToken()).willReturn(REFRESH_TOKEN);
        given(tokenProvider.getRefreshTokenExpiresAt()).willReturn(LocalDateTime.now().plusDays(30));

        // when
        LoginInfo result = authService.login(Provider.KAKAO, "kakao_token");

        // then
        assertThat(result.needsOnboarding()).isFalse();
    }

    @Test
    @DisplayName("유효한 Refresh Token으로 재발급 시 새 Access Token을 발급한다.")
    void refresh_withValidToken_returnsNewAccessToken() {
        // given
        RefreshToken storedToken = RefreshToken.create(USER_ID, REFRESH_TOKEN, LocalDateTime.now().plusDays(20));

        given(refreshTokenRepository.findByToken(REFRESH_TOKEN)).willReturn(Optional.of(storedToken));
        given(tokenProvider.generateAccessToken(USER_ID)).willReturn("new_access_token");
        given(tokenProvider.generateRefreshToken()).willReturn("new_refresh_token");
        given(tokenProvider.getRefreshTokenExpiresAt()).willReturn(LocalDateTime.now().plusDays(30));

        // when
        RefreshInfo result = authService.refresh(REFRESH_TOKEN);

        // then
        assertThat(result.accessToken()).isEqualTo("new_access_token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("만료된 Refresh Token으로 재발급 시 토큰을 삭제하고 예외가 발생한다.")
    void refresh_withExpiredToken_throwsException() {
        // given
        RefreshToken expiredToken = RefreshToken.create(USER_ID, REFRESH_TOKEN, LocalDateTime.now().minusDays(1));

        given(refreshTokenRepository.findByToken(REFRESH_TOKEN)).willReturn(Optional.of(expiredToken));

        // when & then
        assertThatThrownBy(() -> authService.refresh(REFRESH_TOKEN))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("만료된 Refresh Token입니다.");

        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token으로 재발급 시 예외가 발생한다.")
    void refresh_withInvalidToken_throwsException() {
        // given
        given(refreshTokenRepository.findByToken("invalid_token")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.refresh("invalid_token"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("유효하지 않은 Refresh Token입니다.");
    }

    @Test
    @DisplayName("로그아웃 시 Refresh Token을 삭제한다.")
    void logout_success() {
        // when
        authService.logout(USER_ID);

        // then
        verify(refreshTokenRepository).deleteByUserId(USER_ID);
    }
}