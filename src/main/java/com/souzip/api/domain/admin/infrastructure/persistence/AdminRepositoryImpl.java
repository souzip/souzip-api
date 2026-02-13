package com.souzip.api.domain.admin.infrastructure.persistence;

import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.Username;
import com.souzip.api.domain.admin.repository.AdminRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminRepositoryImpl implements AdminRepository {

    private final AdminJpaRepository jpaRepository;
    private final AdminMapper mapper;

    @Override
    public Optional<Admin> findByUsername(Username username) {
        return jpaRepository.findByUsername(username.value())
            .map(mapper::toDomain);
    }

    @Override
    public Optional<Admin> findById(UUID id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }

    @Override
    public Admin save(Admin admin) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(admin)));
    }
}
