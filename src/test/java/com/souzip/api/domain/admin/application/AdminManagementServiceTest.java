package com.souzip.api.domain.admin.application;

import com.souzip.api.domain.admin.application.AdminManagementService.AdminPageResult;
import com.souzip.api.domain.admin.application.command.CreateCityCommand;
import com.souzip.api.domain.admin.application.command.DeleteCityCommand;
import com.souzip.api.domain.admin.application.command.InviteAdminCommand;
import com.souzip.api.domain.admin.application.command.UpdateCityPriorityCommand;
import com.souzip.api.domain.admin.event.AdminCityCreateRequestedEvent;
import com.souzip.api.domain.admin.event.AdminCityDeleteRequestedEvent;
import com.souzip.api.domain.admin.event.AdminCityPriorityChangeRequestedEvent;
import com.souzip.api.domain.admin.exception.AdminErrorCode;
import com.souzip.api.domain.admin.exception.AdminException;
import com.souzip.api.domain.admin.fixture.TestAdminPasswordEncoder;
import com.souzip.api.domain.admin.infrastructure.encoder.AdminPasswordEncoderImpl;
import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.AdminRole;
import com.souzip.api.domain.admin.repository.AdminRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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

    @Mock
    private ApplicationEventPublisher eventPublisher;

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

    @DisplayName("SUPER_ADMIN을 제외한 관리자 목록 조회 성공")
    @Test
    void getAdmins_success() {
        // given
        List<Admin> admins = List.of(
            Admin.create("admin1", "password123", AdminRole.ADMIN, new TestAdminPasswordEncoder()),
            Admin.create("admin2", "password123", AdminRole.VIEWER, new TestAdminPasswordEncoder())
        );

        given(adminRepository.findAllExcludingSuperAdmin(0, 10)).willReturn(admins);
        given(adminRepository.countExcludingSuperAdmin()).willReturn(2L);

        // when
        AdminPageResult result = adminManagementService.getAdmins(1, 10);

        // then
        assertThat(result.admins()).hasSize(2);
        assertThat(result.pageNo()).isEqualTo(1);
        assertThat(result.pageSize()).isEqualTo(10);
        assertThat(result.total()).isEqualTo(2);
        assertThat(result.totalPages()).isEqualTo(1);

        verify(adminRepository).findAllExcludingSuperAdmin(0, 10);
        verify(adminRepository).countExcludingSuperAdmin();
    }

    @DisplayName("관리자 목록 조회 - 2페이지")
    @Test
    void getAdmins_secondPage() {
        // given
        List<Admin> admins = List.of(
            Admin.create("admin11", "password123", AdminRole.ADMIN, new TestAdminPasswordEncoder())
        );

        given(adminRepository.findAllExcludingSuperAdmin(10, 10)).willReturn(admins);
        given(adminRepository.countExcludingSuperAdmin()).willReturn(11L);

        // when
        AdminPageResult result = adminManagementService.getAdmins(2, 10);

        // then
        assertThat(result.admins()).hasSize(1);
        assertThat(result.pageNo()).isEqualTo(2);
        assertThat(result.totalPages()).isEqualTo(2);

        verify(adminRepository).findAllExcludingSuperAdmin(10, 10);
    }

    @DisplayName("관리자 삭제 성공")
    @Test
    void deleteAdmin_success() {
        // given
        UUID adminId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        Admin adminToDelete = Admin.create("admin1", "password123", AdminRole.ADMIN,
            new TestAdminPasswordEncoder());

        given(adminRepository.findById(adminId)).willReturn(Optional.of(adminToDelete));

        // when
        adminManagementService.deleteAdmin(adminId, requesterId);

        // then
        verify(adminRepository).findById(adminId);
        verify(adminRepository).delete(adminToDelete);
    }

    @DisplayName("도시 우선순위 변경 시 이벤트 발행")
    @Test
    void updateCityPriority_publishesEvent() {
        // given
        UpdateCityPriorityCommand command = new UpdateCityPriorityCommand(1L, 1);

        // when
        adminManagementService.updateCityPriority(command);

        // then
        verify(eventPublisher).publishEvent(
            AdminCityPriorityChangeRequestedEvent.of(command.cityId(), command.newPriority())
        );
    }

    @DisplayName("도시 우선순위 초기화 시 이벤트 발행")
    @Test
    void updateCityPriority_reset_publishesEvent() {
        // given
        UpdateCityPriorityCommand command = new UpdateCityPriorityCommand(1L, null);

        // when
        adminManagementService.updateCityPriority(command);

        // then
        verify(eventPublisher).publishEvent(
            AdminCityPriorityChangeRequestedEvent.of(command.cityId(), null)
        );
    }

    @DisplayName("도시 생성 시 이벤트 발행")
    @Test
    void createCity_publishesEvent() {
        // given
        CreateCityCommand command = new CreateCityCommand(
            "Seoul", "서울", 37.56, 126.97, 1L
        );

        // when
        adminManagementService.createCity(command);

        // then
        verify(eventPublisher).publishEvent(
            AdminCityCreateRequestedEvent.of(
                command.nameEn(),
                command.nameKr(),
                command.latitude(),
                command.longitude(),
                command.countryId()
            )
        );
    }

    @DisplayName("도시 삭제 시 이벤트 발행")
    @Test
    void deleteCity_publishesEvent() {
        // given
        DeleteCityCommand command = new DeleteCityCommand(1L);

        // when
        adminManagementService.deleteCity(command);

        // then
        verify(eventPublisher).publishEvent(
            AdminCityDeleteRequestedEvent.of(command.cityId())
        );
    }
}
