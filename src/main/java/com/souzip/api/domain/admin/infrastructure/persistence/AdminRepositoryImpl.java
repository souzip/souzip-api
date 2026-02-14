package com.souzip.api.domain.admin.infrastructure.persistence;

import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.AdminRole;
import com.souzip.api.domain.admin.model.Username;
import com.souzip.api.domain.admin.repository.AdminRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public List<Admin> findAllExcludingSuperAdmin(int offset, int limit) {
        Pageable pageable = PageRequest.of(
            offset / limit,
            limit,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return jpaRepository.findByRoleNot(AdminRole.SUPER_ADMIN, pageable)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public long countExcludingSuperAdmin() {
        return jpaRepository.countByRoleNot(AdminRole.SUPER_ADMIN);
    }

    @Override
    public void delete(Admin admin) {
        jpaRepository.delete(mapper.toEntity(admin));
    }
}
