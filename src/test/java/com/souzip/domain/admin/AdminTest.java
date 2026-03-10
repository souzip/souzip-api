package com.souzip.domain.admin;

import com.souzip.domain.admin.exception.AdminException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.souzip.domain.admin.AdminFixture.createAdmin;
import static com.souzip.domain.admin.AdminFixture.createPasswordEncoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminTest {

    Admin admin;
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = createPasswordEncoder();
        admin = createAdmin(passwordEncoder);
    }

    @DisplayName("어드민을 등록한다")
    @Test
    void register() {
        assertThat(admin.getId()).isNotNull();
        assertThat(admin.getUsername()).isEqualTo("admin123");
        assertThat(admin.getRole()).isEqualTo(AdminRole.ADMIN);
        assertThat(admin.getLastLoginAt()).isNull();
        assertThat(admin.getCreatedAt()).isNotNull();
        assertThat(admin.getUpdatedAt()).isNotNull();
    }

    @DisplayName("로그인 시 마지막 로그인 시간이 업데이트된다")
    @Test
    void login() {
        assertThat(admin.getLastLoginAt()).isNull();

        admin.login();

        assertThat(admin.getLastLoginAt()).isNotNull();
    }

    @DisplayName("비밀번호가 일치하면 true를 반환한다")
    @Test
    void matchesPassword() {
        assertThat(admin.matchesPassword("password123", passwordEncoder)).isTrue();
        assertThat(admin.matchesPassword("wrongpassword", passwordEncoder)).isFalse();
    }

    @DisplayName("비밀번호가 암호화되어 저장된다")
    @Test
    void passwordEncoded() {
        assertThat(admin.getPassword()).isEqualTo("encoded:password123");
    }

    @DisplayName("아이디가 2자 미만이면 예외가 발생한다")
    @Test
    void registerInvalidUsernameFail() {
        assertThatThrownBy(() ->
                Admin.register(AdminRegisterRequest.of("a", "password123", AdminRole.ADMIN), passwordEncoder)
        ).isInstanceOf(AdminException.class);
    }

    @DisplayName("비밀번호가 8자 미만이면 예외가 발생한다")
    @Test
    void registerInvalidPasswordFail() {
        assertThatThrownBy(() ->
                Admin.register(AdminRegisterRequest.of("admin123", "pass", AdminRole.ADMIN), passwordEncoder)
        ).isInstanceOf(AdminException.class);
    }

    @DisplayName("역할이 null이면 예외가 발생한다")
    @Test
    void registerNullRoleFail() {
        assertThatThrownBy(() ->
                Admin.register(AdminRegisterRequest.of("admin123", "password123", null), passwordEncoder)
        ).isInstanceOf(NullPointerException.class);
    }
}