package com.souzip.api.domain.admin.application;

import com.souzip.api.domain.admin.application.command.AdminLoginCommand;
import com.souzip.api.domain.admin.exception.AdminLockedException;
import com.souzip.api.domain.admin.exception.AdminLoginFailedException;
import com.souzip.api.domain.admin.exception.AdminNotFoundException;
import com.souzip.api.domain.admin.fixture.TestAdminPasswordEncoder;
import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.AdminPasswordEncoder;
import com.souzip.api.domain.admin.model.AdminRole;
import com.souzip.api.domain.admin.model.Username;
import com.souzip.api.domain.admin.repository.AdminRepository;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class AdminAuthServiceTest {

    private AdminAuthService adminAuthService;
    private AdminRepository adminRepository;
    private AdminPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        adminRepository = mock(AdminRepository.class);
        passwordEncoder = mock(AdminPasswordEncoder.class);
        adminAuthService = new AdminAuthService(adminRepository, passwordEncoder);
    }

    @Test
    @DisplayName("로그인에 성공한다.")
    void login_success() {
        // given
        AdminLoginCommand command = new AdminLoginCommand("admin123", "password123");
        Admin admin = Admin.create("admin123", "password123", AdminRole.SUPER_ADMIN,
            new TestAdminPasswordEncoder());

        given(adminRepository.findByUsername(any(Username.class))).willReturn(Optional.of(admin));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(adminRepository.save(any(Admin.class))).willReturn(admin);

        // when
        Admin result = adminAuthService.login(command);

        // then
        assertThat(result.getLoginFailCount()).isZero();
        assertThat(result.getLastLoginAt()).isNotNull();
        verify(adminRepository, times(1)).save(admin);
    }

    @Test
    @DisplayName("존재하지 않는 계정으로 로그인 시 예외가 발생한다.")
    void login_fail_not_found() {
        // given
        AdminLoginCommand command = new AdminLoginCommand("admin123", "password123");
        given(adminRepository.findByUsername(any(Username.class))).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminAuthService.login(command))
            .isInstanceOf(AdminNotFoundException.class);
    }

    @Test
    @DisplayName("잠긴 계정으로 로그인 시 예외가 발생한다.")
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

    @Test
    @DisplayName("비밀번호 불일치 시 예외가 발생하고 실패 횟수가 증가한다.")
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

    private void lockAdmin(Admin admin) {
        IntStream.range(0, 5).forEach(i -> admin.recordLoginFailure());
    }
}
