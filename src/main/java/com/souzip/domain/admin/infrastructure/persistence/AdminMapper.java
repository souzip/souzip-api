package com.souzip.domain.admin.infrastructure.persistence;

import com.souzip.domain.admin.infrastructure.entity.AdminEntity;
import com.souzip.domain.admin.model.Admin;
import com.souzip.domain.admin.model.Password;
import com.souzip.domain.admin.model.Username;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

    public Admin toDomain(AdminEntity entity) {
        return Admin.restore(
            entity.getId(),
            new Username(entity.getUsername()),
            Password.of(entity.getPassword()),
            entity.getRole(),
            entity.getLastLoginAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public AdminEntity toEntity(Admin admin) {
        return AdminEntity.builder()
            .id(admin.getId())
            .username(admin.getUsername().value())
            .password(admin.getPassword().getEncodedValue())
            .role(admin.getRole())
            .lastLoginAt(admin.getLastLoginAt())
            .build();
    }
}
