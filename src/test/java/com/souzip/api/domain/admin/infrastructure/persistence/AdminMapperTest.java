package com.souzip.api.domain.admin.infrastructure.persistence;

import com.souzip.api.domain.admin.fixture.TestAdminPasswordEncoder;
import com.souzip.api.domain.admin.infrastructure.entity.AdminEntity;
import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.AdminRole;
import com.souzip.api.domain.admin.model.AdminPasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AdminMapperTest {

    private AdminMapper mapper;
    private AdminPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mapper = new AdminMapper();
        passwordEncoder = new TestAdminPasswordEncoder();
    }

    @DisplayName("Admin 도메인을 AdminJpaEntity로 변환에 성공한다.")
    @Test
    void toEntity_success() {
        // given
        Admin admin = Admin.create("admin123", "password123", AdminRole.SUPER_ADMIN, passwordEncoder);

        // when
        AdminEntity entity = mapper.toEntity(admin);

        // then
        assertThat(entity.getUsername()).isEqualTo("admin123");
        assertThat(entity.getRole()).isEqualTo(AdminRole.SUPER_ADMIN);
        assertThat(entity.getLoginFailCount()).isZero();
    }

    @DisplayName("AdminJpaEntity를 Admin 도메인으로 변환에 성공한다.")
    @Test
    void toDomain_success() {
        // given
        AdminEntity entity = AdminEntity.builder()
            .id(UUID.randomUUID())
            .username("admin123")
            .password("encoded_password123")
            .role(AdminRole.SUPER_ADMIN)
            .loginFailCount(0)
            .build();

        // when
        Admin admin = mapper.toDomain(entity);

        // then
        assertThat(admin.getUsername().value()).isEqualTo("admin123");
        assertThat(admin.getRole()).isEqualTo(AdminRole.SUPER_ADMIN);
        assertThat(admin.getLoginFailCount()).isZero();
    }
}
