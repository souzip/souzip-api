package com.souzip.api.domain.admin.infrastructure.persistence;

import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.Username;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminRepositoryImpl implements com.souzip.api.domain.admin.repository.AdminRepository {

    private final AdminRepository jpaRepository;
    private final AdminMapper mapper;

    @Override
    public Optional<Admin> findByUsername(Username username) {
        return jpaRepository.findByUsername(username.value())
            .map(mapper::toDomain);
    }

    @Override
    public Admin save(Admin admin) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(admin)));
    }
}
