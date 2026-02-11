package com.souzip.api.domain.admin.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminTest {

    private AdminPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = rawPassword -> "encoded_" + rawPassword;
    }

    @Test
    @DisplayName("Admin 생성에 성공한다.")
    void create_success() {
        Admin admin = Admin.create("admin123", "password123", AdminRole.SUPER_ADMIN, passwordEncoder);

        assertThat(admin.getId()).isNotNull();
        assertThat(admin.getUsername().value()).isEqualTo("admin123");
        assertThat(admin.getRole()).isEqualTo(AdminRole.SUPER_ADMIN);
        assertThat(admin.getLoginFailCount()).isZero();
        assertThat(admin.getLockedAt()).isNull();
        assertThat(admin.getCreatedAt()).isNotNull();
    }
}
