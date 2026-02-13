package com.souzip.api.domain.admin.infrastructure.persistence;

import com.souzip.api.domain.admin.infrastructure.entity.AdminRefreshTokenEntity;
import com.souzip.api.domain.admin.model.AdminRefreshToken;
import org.springframework.stereotype.Component;

@Component
public class AdminRefreshTokenMapper {

    public AdminRefreshTokenEntity toEntity(AdminRefreshToken domain) {
        return AdminRefreshTokenEntity.builder()
            .id(domain.getId())
            .adminId(domain.getAdminId())
            .token(domain.getToken())
            .expiresAt(domain.getExpiresAt())
            .build();
    }

    public AdminRefreshToken toDomain(AdminRefreshTokenEntity entity) {
        return AdminRefreshToken.restore(
            entity.getId(),
            entity.getAdminId(),
            entity.getToken(),
            entity.getExpiresAt(),
            entity.getCreatedAt()
        );
    }
}
