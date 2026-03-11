package com.souzip.application.admin.required;

import com.souzip.domain.admin.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminRepository extends Repository<Admin, UUID> {
    Optional<Admin> findByUsername(String username);

    Optional<Admin> findById(UUID id);

    Admin save(Admin admin);

    boolean existsByUsername(String username);

    void delete(Admin admin);

    Page<Admin> findAll(Pageable pageable);

    @Query("SELECT a FROM Admin a WHERE a.id IN :ids")
    List<Admin> findAllByIds(List<UUID> ids);
}