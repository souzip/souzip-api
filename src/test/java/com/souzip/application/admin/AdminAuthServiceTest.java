package com.souzip.application.admin;

import com.souzip.application.admin.dto.AdminLoginResult;
import com.souzip.application.admin.dto.AdminRefreshResult;
import com.souzip.application.admin.required.AdminRefreshTokenRepository;
import com.souzip.application.admin.required.AdminRepository;
import com.souzip.application.admin.required.TokenProvider;
import com.souzip.domain.admin.Admin;
import com.souzip.domain.admin.AdminFixture;
import com.souzip.domain.admin.AdminRefreshToken;
import com.souzip.domain.admin.PasswordEncoder;
import com.souzip.domain.admin.exception.AdminExpiredRefreshTokenException;
import com.souzip.domain.admin.exception.AdminInvalidRefreshTokenException;
import com.souzip.domain.admin.exception.AdminLoginFailedException;
import com.souzip.domain.admin.exception.AdminNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AdminRefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Spy
    private final PasswordEncoder passwordEncoder = AdminFixture.createPasswordEncoder();

    @InjectMocks
    private AdminAuthService adminAuthService;

    @DisplayName("로그인 성공 시 액세스 토큰과 리프레시 토큰을 반환한다")
    @Test
    void login_success() {
        Admin admin = AdminFixture.createAdmin();
        given(adminRepository.findByUsername("admin123")).willReturn(Optional.of(admin));
        given(adminRepository.save(any())).willReturn(admin);
        given(refreshTokenRepository.findByAdminId(any())).willReturn(Optional.empty());
        given(tokenProvider.generateAccessToken(any())).willReturn("access-token");
        given(tokenProvider.generateRefreshToken(any())).willReturn("refresh-token");

        AdminLoginResult result = adminAuthService.login("admin123", "password123");

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.admin().getUsername()).isEqualTo("admin123");
    }

    @DisplayName("존재하지 않는 어드민으로 로그인 시 예외가 발생한다")
    @Test
    void login_adminNotFound() {
        given(adminRepository.findByUsername(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminAuthService.login("admin123", "password123"))
                .isInstanceOf(AdminNotFoundException.class);
    }

    @DisplayName("비밀번호가 틀리면 예외가 발생한다")
    @Test
    void login_wrongPassword() {
        Admin admin = AdminFixture.createAdmin();
        given(adminRepository.findByUsername("admin123")).willReturn(Optional.of(admin));

        assertThatThrownBy(() -> adminAuthService.login("admin123", "wrongpassword"))
                .isInstanceOf(AdminLoginFailedException.class);
    }

    @DisplayName("유효한 리프레시 토큰으로 액세스 토큰을 재발급한다")
    @Test
    void refresh_success() {
        Admin admin = AdminFixture.createAdmin();
        AdminRefreshToken refreshToken = AdminFixture.createRefreshToken(admin.getId());
        given(refreshTokenRepository.findByToken("refresh-token")).willReturn(Optional.of(refreshToken));
        given(adminRepository.findById(any())).willReturn(Optional.of(admin));
        given(tokenProvider.generateAccessToken(any())).willReturn("new-access-token");

        AdminRefreshResult result = adminAuthService.refresh("refresh-token");

        assertThat(result.accessToken()).isEqualTo("new-access-token");
    }

    @DisplayName("유효하지 않은 리프레시 토큰으로 재발급 시 예외가 발생한다")
    @Test
    void refresh_invalidToken() {
        given(refreshTokenRepository.findByToken(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminAuthService.refresh("invalid-token"))
                .isInstanceOf(AdminInvalidRefreshTokenException.class);
    }

    @DisplayName("만료된 리프레시 토큰으로 재발급 시 예외가 발생한다")
    @Test
    void refresh_expiredToken() {
        Admin admin = AdminFixture.createAdmin();
        AdminRefreshToken expiredToken = AdminFixture.createExpiredRefreshToken(admin.getId());
        given(refreshTokenRepository.findByToken("expired-token")).willReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> adminAuthService.refresh("expired-token"))
                .isInstanceOf(AdminExpiredRefreshTokenException.class);

        verify(refreshTokenRepository).delete(expiredToken);
    }

    @DisplayName("로그아웃 시 리프레시 토큰이 삭제된다")
    @Test
    void logout_success() {
        UUID adminId = UUID.randomUUID();
        AdminRefreshToken refreshToken = AdminFixture.createRefreshToken(adminId);
        given(refreshTokenRepository.findByAdminId(adminId)).willReturn(Optional.of(refreshToken));

        adminAuthService.logout(adminId);

        verify(refreshTokenRepository).delete(refreshToken);
    }
}