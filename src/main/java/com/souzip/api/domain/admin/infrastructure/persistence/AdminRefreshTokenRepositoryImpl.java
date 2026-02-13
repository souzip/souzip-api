package com.souzip.api.domain.admin.infrastructure.persistence;

import com.souzip.api.domain.admin.model.AdminRefreshToken;
import com.souzip.api.domain.admin.repository.AdminRefreshTokenRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class AdminRefreshTokenRepositoryImpl implements AdminRefreshTokenRepository {

    private final AdminRefreshTokenJpaRepository jpaRepository;
    private final AdminRefreshTokenMapper mapper;

    @Override
    public AdminRefreshToken save(AdminRefreshToken refreshToken) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(refreshToken)));
    }

    @Override
    public Optional<AdminRefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
            .map(mapper::toDomain);
    }

    @Override
    public Optional<AdminRefreshToken> findByAdminId(UUID adminId) {
        return jpaRepository.findByAdminId(adminId)
            .map(mapper::toDomain);
    }

    @Override
    public void delete(AdminRefreshToken refreshToken) {
        jpaRepository.delete(mapper.toEntity(refreshToken));
    }

    @Override
    public int deleteAllByExpiresAtBefore(LocalDateTime dateTime) {
        return jpaRepository.deleteAllByExpiresAtBefore(dateTime);
    }
}
