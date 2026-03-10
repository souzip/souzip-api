package com.souzip.application.admin;

import com.souzip.application.admin.required.AdminRepository;
import com.souzip.domain.admin.Admin;
import com.souzip.domain.admin.AdminFixture;
import com.souzip.domain.admin.exception.AdminNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminQueryServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private AdminQueryService adminQueryService;

    @DisplayName("ID로 어드민을 조회한다")
    @Test
    void findById_success() {
        UUID adminId = UUID.randomUUID();
        Admin admin = AdminFixture.createAdmin();
        given(adminRepository.findById(adminId)).willReturn(Optional.of(admin));

        Admin result = adminQueryService.findById(adminId);

        assertThat(result.getUsername()).isEqualTo("admin123");
    }

    @DisplayName("존재하지 않는 어드민 조회 시 예외가 발생한다")
    @Test
    void findById_notFound() {
        UUID adminId = UUID.randomUUID();
        given(adminRepository.findById(adminId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminQueryService.findById(adminId))
                .isInstanceOf(AdminNotFoundException.class);
    }

    @DisplayName("어드민 목록을 페이지네이션으로 조회한다")
    @Test
    void findAll_success() {
        PageRequest pageable = PageRequest.of(0, 10);
        List<Admin> admins = List.of(AdminFixture.createAdmin(), AdminFixture.createAdmin("admin456"));
        Page<Admin> page = new PageImpl<>(admins, pageable, 2);
        given(adminRepository.findAll(pageable)).willReturn(page);

        Page<Admin> result = adminQueryService.findAll(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }
}