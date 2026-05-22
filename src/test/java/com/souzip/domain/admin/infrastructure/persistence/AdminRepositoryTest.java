package com.souzip.domain.admin.infrastructure.persistence;

import com.souzip.domain.admin.fixture.TestAdminPasswordEncoder;
import com.souzip.domain.admin.model.Admin;
import com.souzip.domain.admin.model.AdminPasswordEncoder;
import com.souzip.domain.admin.model.AdminRole;
import com.souzip.domain.admin.model.Username;
import com.souzip.domain.admin.repository.AdminRepository;
import com.souzip.global.config.QuerydslConfig;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({AdminRepositoryImpl.class, AdminMapper.class, QuerydslConfig.class})
class AdminRepositoryTest {

    @Autowired
    private AdminRepository adminRepository;

    private AdminPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new TestAdminPasswordEncoder();
    }

    @DisplayName("Admin 저장에 성공한다.")
    @Test
    void save_success() {
        // given
        Admin admin = Admin.create("admin123", "password123", AdminRole.SUPER_ADMIN, passwordEncoder);

        // when
        Admin saved = adminRepository.save(admin);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername().value()).isEqualTo("admin123");
    }

    @DisplayName("username으로 Admin 조회에 성공한다.")
    @Test
    void findByUsername_success() {
        // given
        Admin admin = Admin.create("admin123", "password123", AdminRole.SUPER_ADMIN, passwordEncoder);
        adminRepository.save(admin);

        // when
        Optional<Admin> found = adminRepository.findByUsername(new Username("admin123"));

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername().value()).isEqualTo("admin123");
    }

    @DisplayName("존재하지 않는 username 조회 시 빈 값을 반환한다.")
    @Test
    void findByUsername_not_found() {
        // when
        Optional<Admin> found = adminRepository.findByUsername(new Username("admin123"));

        // then
        assertThat(found).isEmpty();
    }

    @DisplayName("username 존재 여부 확인 - 존재함")
    @Test
    void existsByUsername_exists() {
        // given
        Admin admin = Admin.create("testadmin", "password123", AdminRole.ADMIN, passwordEncoder);
        adminRepository.save(admin);

        // when
        boolean exists = adminRepository.existsByUsername("testadmin");

        // then
        assertThat(exists).isTrue();
    }

    @DisplayName("username 존재 여부 확인 - 존재하지 않음")
    @Test
    void existsByUsername_notExists() {
        // when
        boolean exists = adminRepository.existsByUsername("nonexistent");

        // then
        assertThat(exists).isFalse();
    }

    @DisplayName("SUPER_ADMIN을 제외한 Admin 목록을 페이징으로 조회한다.")
    @Test
    void findAllExcludingSuperAdmin_withPagination_success() {
        // given
        adminRepository.save(Admin.create("superadmin", "password123", AdminRole.SUPER_ADMIN, passwordEncoder));

        for (int i = 1; i <= 15; i++) {
            Admin admin = Admin.create("admin" + i, "password123", AdminRole.ADMIN, passwordEncoder);
            adminRepository.save(admin);
        }

        // when
        List<Admin> firstPage = adminRepository.findAllExcludingSuperAdmin(0, 10);
        List<Admin> secondPage = adminRepository.findAllExcludingSuperAdmin(10, 10);

        // then
        assertThat(firstPage).hasSize(10);
        assertThat(secondPage).hasSize(5);
        assertThat(firstPage).noneMatch(admin -> admin.getRole() == AdminRole.SUPER_ADMIN);
        assertThat(secondPage).noneMatch(admin -> admin.getRole() == AdminRole.SUPER_ADMIN);
    }

    @DisplayName("SUPER_ADMIN을 제외한 Admin 개수를 조회한다.")
    @Test
    void countExcludingSuperAdmin_success() {
        // given
        adminRepository.save(Admin.create("superadmin1", "password123", AdminRole.SUPER_ADMIN, passwordEncoder));
        adminRepository.save(Admin.create("superadmin2", "password123", AdminRole.SUPER_ADMIN, passwordEncoder));

        for (int i = 1; i <= 5; i++) {
            Admin admin = Admin.create("admin" + i, "password123", AdminRole.ADMIN, passwordEncoder);
            adminRepository.save(admin);
        }

        // when
        long count = adminRepository.countExcludingSuperAdmin();

        // then
        assertThat(count).isEqualTo(5);
    }

    @DisplayName("Admin 삭제에 성공한다.")
    @Test
    void delete_success() {
        // given
        Admin admin = Admin.create("admin123", "password123", AdminRole.ADMIN, passwordEncoder);
        Admin saved = adminRepository.save(admin);

        // when
        adminRepository.delete(saved);

        // then
        Optional<Admin> found = adminRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @DisplayName("SUPER_ADMIN을 제외한 Admin 목록 조회 시 빈 리스트를 반환한다.")
    @Test
    void findAllExcludingSuperAdmin_emptyResult() {
        // given
        adminRepository.save(Admin.create("superadmin", "password123", AdminRole.SUPER_ADMIN, passwordEncoder));

        // when
        List<Admin> result = adminRepository.findAllExcludingSuperAdmin(0, 10);

        // then
        assertThat(result).isEmpty();
    }
}
