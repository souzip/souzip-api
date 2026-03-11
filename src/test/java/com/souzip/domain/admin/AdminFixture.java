package com.souzip.domain.admin;

import java.time.LocalDateTime;
import java.util.UUID;

public class AdminFixture {

    public static Admin createAdmin() {
        return Admin.register(createAdminRegisterRequest(), createPasswordEncoder());
    }

    public static Admin createAdmin(PasswordEncoder passwordEncoder) {
        return Admin.register(createAdminRegisterRequest(), passwordEncoder);
    }

    public static Admin createAdmin(AdminRole role) {
        return Admin.register(createAdminRegisterRequest(role), createPasswordEncoder());
    }

    public static Admin createAdmin(String username) {
        return Admin.register(createAdminRegisterRequest(username), createPasswordEncoder());
    }

    public static AdminRegisterRequest createAdminRegisterRequest() {
        return AdminRegisterRequest.of("admin123", "password123", AdminRole.ADMIN);
    }

    public static AdminRegisterRequest createAdminRegisterRequest(AdminRole role) {
        return AdminRegisterRequest.of("admin123", "password123", role);
    }

    public static AdminRegisterRequest createAdminRegisterRequest(String username) {
        return AdminRegisterRequest.of(username, "password123", AdminRole.ADMIN);
    }

    public static AdminRefreshToken createRefreshToken(UUID adminId) {
        return AdminRefreshToken.create(adminId, "refresh-token", LocalDateTime.now().plusDays(30));
    }

    public static AdminRefreshToken createExpiredRefreshToken(UUID adminId) {
        return AdminRefreshToken.create(adminId, "refresh-token", LocalDateTime.now().minusDays(1));
    }

    public static PasswordEncoder createPasswordEncoder() {
        return new FakePasswordEncoder();
    }

    public static class FakePasswordEncoder implements PasswordEncoder {
        @Override
        public String encode(String password) {
            return "encoded:" + password;
        }

        @Override
        public boolean matches(String password, String encodedPassword) {
            return encodedPassword.equals("encoded:" + password);
        }
    }
}