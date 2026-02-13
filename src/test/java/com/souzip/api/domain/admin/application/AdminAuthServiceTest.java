package com.souzip.api.domain.admin.application;

import com.souzip.api.domain.admin.application.AdminAuthService.AdminLoginResult;
import com.souzip.api.domain.admin.application.AdminAuthService.RefreshResult;
import com.souzip.api.domain.admin.application.command.AdminLoginCommand;
import com.souzip.api.domain.admin.exception.*;
import com.souzip.api.domain.admin.fixture.TestAdminPasswordEncoder;
import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.AdminPasswordEncoder;
import com.souzip.api.domain.admin.model.AdminRefreshToken;
import com.souzip.api.domain.admin.model.AdminRole;
import com.souzip.api.domain.admin.model.Username;
import com.souzip.api.domain.admin.repository.AdminRefreshTokenRepository;
import com.souzip.api.domain.admin.repository.AdminRepository;
import com.souzip.api.global.security.jwt.JwtTokenProvider;
import java.time.LocalDateTime;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class AdminAuthServiceTest {

    private AdminAuthService adminAuthService;
    private AdminRepository adminRepository;
    private AdminRefreshTokenRepository refreshTokenRepository;
    private AdminPasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        adminRepository = mock(AdminRepository.class);
        refreshTokenRepository = mock(AdminRefreshTokenRepository.class);
        passwordEncoder = mock(AdminPasswordEncoder.class);
        jwtTokenProvider = mock(JwtTokenProvider.class);
        adminAuthService = new AdminAuthService(adminRepository, refreshTokenRepository, passwordEncoder, jwtTokenProvider);
    }

    @DisplayName("로그인에 성공한다.")
    @Test
    void login_success() {
        // given
        AdminLoginCommand command = new AdminLoginCommand("admin123", "password123");
        Admin admin = Admin.create("admin123", "password123", AdminRole.SUPER_ADMIN,
            new TestAdminPasswordEncoder());

        given(adminRepository.findByUsername(any(Username.class))).willReturn(Optional.of(admin));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(adminRepository.save(any(Admin.class))).willReturn(admin);
        given(jwtTokenProvider.generateToken(anyString())).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken(anyString())).willReturn("refresh-token");
        given(refreshTokenRepository.findByAdminId(any(UUID.class))).willReturn(Optional.empty());

        // when
        AdminLoginResult result = adminAuthService.login(command);

        // then
        assertThat(result.admin().getLoginFailCount()).isZero();
        assertThat(result.admin().getLastLoginAt()).isNotNull();
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        verify(adminRepository, times(1)).save(admin);
        verify(refreshTokenRepository, times(1)).save(any(AdminRefreshToken.class));
    }

    @DisplayName("존재하지 않는 계정으로 로그인 시 예외가 발생한다.")
    @Test
    void login_fail_not_found() {
        // given
        AdminLoginCommand command = new AdminLoginCommand("admin123", "password123");
        given(adminRepository.findByUsername(any(Username.class))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminAuthService.login(command))
            .isInstanceOf(AdminNotFoundException.class);
    }

    @DisplayName("잠긴 계정으로 로그인 시 예외가 발생한다.")
    @Test
    void login_fail_locked() {
        // given
        AdminLoginCommand command = new AdminLoginCommand("admin123", "password123");
        Admin admin = Admin.create("admin123", "password123", AdminRole.SUPER_ADMIN,
            new TestAdminPasswordEncoder());

        lockAdmin(admin);

        given(adminRepository.findByUsername(any(Username.class))).willReturn(Optional.of(admin));

        // when & then
        assertThatThrownBy(() -> adminAuthService.login(command))
            .isInstanceOf(AdminLockedException.class);
    }

    @DisplayName("비밀번호 불일치 시 예외가 발생하고 실패 횟수가 증가한다.")
    @Test
    void login_fail_password_mismatch() {
        // given
        AdminLoginCommand command = new AdminLoginCommand("admin123", "wrongpassword");
        Admin admin = Admin.create("admin123", "password123", AdminRole.SUPER_ADMIN,
            new TestAdminPasswordEncoder());

        given(adminRepository.findByUsername(any(Username.class))).willReturn(Optional.of(admin));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);
        given(adminRepository.save(any(Admin.class))).willReturn(admin);

        // when & then
        assertThatThrownBy(() -> adminAuthService.login(command))
            .isInstanceOf(AdminLoginFailedException.class);

        verify(adminRepository, times(1)).save(admin);
    }

    @DisplayName("리프레시 토큰 갱신에 성공한다.")
    @Test
    void refresh_success() {
        // given
        UUID adminId = UUID.randomUUID();
        Admin admin = Admin.create("admin123", "password123", AdminRole.SUPER_ADMIN,
            new TestAdminPasswordEncoder());
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        AdminRefreshToken refreshToken = AdminRefreshToken.create(adminId, "refresh-token", expiresAt);

        given(refreshTokenRepository.findByToken("refresh-token")).willReturn(Optional.of(refreshToken));
        given(adminRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(jwtTokenProvider.generateToken(anyString())).willReturn("new-access-token");

        // when
        RefreshResult result = adminAuthService.refresh("refresh-token");

        // then
        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
    }

    @DisplayName("유효하지 않은 리프레시 토큰으로 갱신 시 예외가 발생한다.")
    @Test
    void refresh_fail_invalid_token() {
        // given
        given(refreshTokenRepository.findByToken("invalid-token")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminAuthService.refresh("invalid-token"))
            .isInstanceOf(AdminInvalidRefreshTokenException.class);
    }

    @DisplayName("만료된 리프레시 토큰으로 갱신 시 예외가 발생한다.")
    @Test
    void refresh_fail_expired_token() {
        // given
        UUID adminId = UUID.randomUUID();
        LocalDateTime expiredAt = LocalDateTime.now().minusDays(1);
        AdminRefreshToken refreshToken = AdminRefreshToken.create(adminId, "refresh-token", expiredAt);

        given(refreshTokenRepository.findByToken("refresh-token")).willReturn(Optional.of(refreshToken));

        // when & then
        assertThatThrownBy(() -> adminAuthService.refresh("refresh-token"))
            .isInstanceOf(AdminExpiredRefreshTokenException.class);

        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @DisplayName("만료 임박한 리프레시 토큰은 갱신된다.")
    @Test
    void refresh_renew_expiring_token() {
        // given
        UUID adminId = UUID.randomUUID();
        Admin admin = Admin.create("admin123", "password123", AdminRole.SUPER_ADMIN,
            new TestAdminPasswordEncoder());
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(5); // 10일 미만
        AdminRefreshToken refreshToken = AdminRefreshToken.create(adminId, "old-refresh-token", expiresAt);

        given(refreshTokenRepository.findByToken("old-refresh-token")).willReturn(Optional.of(refreshToken));
        given(adminRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(jwtTokenProvider.generateToken(anyString())).willReturn("new-access-token");
        given(jwtTokenProvider.generateRefreshToken(anyString())).willReturn("new-refresh-token");

        // when
        RefreshResult result = adminAuthService.refresh("old-refresh-token");

        // then
        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
        verify(refreshTokenRepository, times(1)).save(refreshToken);
    }

    @DisplayName("로그아웃에 성공한다.")
    @Test
    void logout_success() {
        // given
        UUID adminId = UUID.randomUUID();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        AdminRefreshToken refreshToken = AdminRefreshToken.create(adminId, "refresh-token", expiresAt);

        given(refreshTokenRepository.findByAdminId(adminId)).willReturn(Optional.of(refreshToken));

        // when
        adminAuthService.logout(adminId);

        // then
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
    }

    @DisplayName("리프레시 토큰이 없어도 로그아웃에 성공한다.")
    @Test
    void logout_success_without_token() {
        // given
        UUID adminId = UUID.randomUUID();
        given(refreshTokenRepository.findByAdminId(adminId)).willReturn(Optional.empty());

        // when
        adminAuthService.logout(adminId);

        // then
        verify(refreshTokenRepository, never()).delete(any());
    }

    private void lockAdmin(Admin admin) {
        IntStream.range(0, 5).forEach(i -> admin.recordLoginFailure());
    }
}
