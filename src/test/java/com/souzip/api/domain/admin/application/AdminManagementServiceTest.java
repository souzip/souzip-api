// src/test/java/com/souzip/api/domain/admin/application/AdminManagementServiceTest.java
package com.souzip.api.domain.admin.application;

import com.souzip.api.domain.admin.application.command.InviteAdminCommand;
import com.souzip.api.domain.admin.exception.AdminErrorCode;
import com.souzip.api.domain.admin.exception.AdminException;
import com.souzip.api.domain.admin.infrastructure.encoder.AdminPasswordEncoderImpl;
import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.AdminRole;
import com.souzip.api.domain.admin.repository.AdminRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminManagementServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AdminPasswordEncoderImpl passwordEncoder;

    @InjectMocks
    private AdminManagementService adminManagementService;

    @DisplayName("ADMIN 역할 관리자 초대 성공")
    @Test
    void inviteAdmin_withAdminRole_success() {
        // given
        InviteAdminCommand command = new InviteAdminCommand(
            "newadmin",
            "password123",
            AdminRole.ADMIN
        );

        given(passwordEncoder.encode(anyString())).willReturn("encoded_password123");
        given(adminRepository.existsByUsername(anyString())).willReturn(false);
        given(adminRepository.save(any(Admin.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Admin result = adminManagementService.inviteAdmin(command);

        // then
        assertThat(result.getUsername().value()).isEqualTo("newadmin");
        assertThat(result.getRole()).isEqualTo(AdminRole.ADMIN);
        assertThat(result.getLoginFailCount()).isZero();
        assertThat(result.getLockedAt()).isNull();
        assertThat(result.getLastLoginAt()).isNull();

        verify(adminRepository).existsByUsername("newadmin");
        verify(adminRepository).save(any(Admin.class));
        verify(passwordEncoder).encode("password123");
    }

    @DisplayName("VIEWER 역할 관리자 초대 성공")
    @Test
    void inviteAdmin_withViewerRole_success() {
        // given
        InviteAdminCommand command = new InviteAdminCommand(
            "viewer01",
            "password123",
            AdminRole.VIEWER
        );

        given(passwordEncoder.encode(anyString())).willReturn("encoded_password123");
        given(adminRepository.existsByUsername(anyString())).willReturn(false);
        given(adminRepository.save(any(Admin.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Admin result = adminManagementService.inviteAdmin(command);

        // then
        assertThat(result.getUsername().value()).isEqualTo("viewer01");
        assertThat(result.getRole()).isEqualTo(AdminRole.VIEWER);
        assertThat(result.getLoginFailCount()).isZero();

        verify(adminRepository).existsByUsername("viewer01");
        verify(adminRepository).save(any(Admin.class));
        verify(passwordEncoder).encode("password123");
    }

    @DisplayName("SUPER_ADMIN 역할 초대 시도 시 예외 발생")
    @Test
    void inviteAdmin_withSuperAdminRole_throwsException() {
        // given
        InviteAdminCommand command = new InviteAdminCommand(
            "superadmin",
            "password123",
            AdminRole.SUPER_ADMIN
        );

        // when & then
        assertThatThrownBy(() -> adminManagementService.inviteAdmin(command))
            .isInstanceOf(AdminException.class)
            .hasMessage(AdminErrorCode.CANNOT_INVITE_SUPER_ADMIN.getMessage());

        verify(adminRepository, never()).existsByUsername(anyString());
        verify(adminRepository, never()).save(any(Admin.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @DisplayName("중복된 username으로 초대 시도 시 예외 발생")
    @Test
    void inviteAdmin_withDuplicateUsername_throwsException() {
        // given
        InviteAdminCommand command = new InviteAdminCommand(
            "existing",
            "password123",
            AdminRole.ADMIN
        );

        given(adminRepository.existsByUsername("existing")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> adminManagementService.inviteAdmin(command))
            .isInstanceOf(AdminException.class)
            .hasMessage(AdminErrorCode.ADMIN_USERNAME_DUPLICATED.getMessage());

        verify(adminRepository).existsByUsername("existing");
        verify(adminRepository, never()).save(any(Admin.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @DisplayName("비밀번호가 암호화되어 저장됨")
    @Test
    void inviteAdmin_passwordIsEncoded() {
        // given
        InviteAdminCommand command = new InviteAdminCommand(
            "newadmin",
            "rawPassword123",
            AdminRole.ADMIN
        );

        given(passwordEncoder.encode("rawPassword123")).willReturn("encoded_rawPassword123");
        given(adminRepository.existsByUsername(anyString())).willReturn(false);
        given(adminRepository.save(any(Admin.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Admin result = adminManagementService.inviteAdmin(command);

        // then
        verify(passwordEncoder).encode("rawPassword123");
        assertThat(result.getPassword().getEncodedValue()).isEqualTo("encoded_rawPassword123");
    }
}
