package com.souzip.api.domain.admin.infrastructure.persistence;

import com.souzip.api.domain.admin.fixture.TestAdminPasswordEncoder;
import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.AdminPasswordEncoder;
import com.souzip.api.domain.admin.model.AdminRole;
import com.souzip.api.domain.admin.model.Username;
import com.souzip.api.domain.admin.repository.AdminRepository;
import com.souzip.api.global.config.QuerydslConfig;
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
}
