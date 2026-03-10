package com.souzip.domain.admin;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Admin {

    private UUID id;

    private String username;

    private String password;

    private AdminRole role;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static Admin register(AdminRegisterRequest request, PasswordEncoder passwordEncoder) {
        Admin admin = new Admin();

        admin.id = UUID.randomUUID();
        admin.username = request.username();
        admin.password = passwordEncoder.encode(request.password());
        admin.role = request.role();
        admin.createdAt = LocalDateTime.now();
        admin.updatedAt = LocalDateTime.now();

        return admin;
    }

    public void login() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public boolean matchesPassword(String password, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(password, this.password);
    }
}