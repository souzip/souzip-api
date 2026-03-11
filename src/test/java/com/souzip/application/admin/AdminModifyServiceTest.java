package com.souzip.application.admin;

import com.souzip.application.admin.provided.AdminFinder;
import com.souzip.application.admin.required.AdminRepository;
import com.souzip.domain.admin.*;
import com.souzip.domain.admin.exception.AdminErrorCode;
import com.souzip.domain.admin.exception.AdminException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminModifyServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AdminFinder adminFinder;

    @Spy
    private final PasswordEncoder passwordEncoder = AdminFixture.createPasswordEncoder();

    @InjectMocks
    private AdminModifyService adminModifyService;

    @DisplayName("어드민을 등록한다")
    @Test
    void register_success() {
        AdminRegisterRequest request = AdminFixture.createAdminRegisterRequest();
        given(adminRepository.existsByUsername(request.username())).willReturn(false);
        given(adminRepository.save(any())).willAnswer(invocation -> invocation.getArgument(0));

        Admin result = adminModifyService.register(request);

        assertThat(result.getUsername()).isEqualTo("admin123");
        assertThat(result.getRole()).isEqualTo(AdminRole.ADMIN);
    }

    @DisplayName("중복된 아이디로 등록 시 예외가 발생한다")
    @Test
    void register_duplicateUsername() {
        AdminRegisterRequest request = AdminFixture.createAdminRegisterRequest();
        given(adminRepository.existsByUsername(request.username())).willReturn(true);

        assertThatThrownBy(() -> adminModifyService.register(request))
                .isInstanceOf(AdminException.class)
                .hasMessage(AdminErrorCode.ADMIN_USERNAME_DUPLICATED.getMessage());

        verify(adminRepository, never()).save(any());
    }

    @DisplayName("슈퍼관리자 등록 시 예외가 발생한다")
    @Test
    void register_superAdmin() {
        AdminRegisterRequest request = AdminFixture.createAdminRegisterRequest(AdminRole.SUPER_ADMIN);

        assertThatThrownBy(() -> adminModifyService.register(request))
                .isInstanceOf(AdminException.class)
                .hasMessage(AdminErrorCode.CANNOT_INVITE_SUPER_ADMIN.getMessage());

        verify(adminRepository, never()).save(any());
    }

    @DisplayName("어드민을 삭제한다")
    @Test
    void delete_success() {
        UUID adminId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        Admin admin = AdminFixture.createAdmin();
        given(adminFinder.findById(adminId)).willReturn(admin);

        adminModifyService.delete(adminId, requesterId);

        verify(adminRepository).delete(admin);
    }
}