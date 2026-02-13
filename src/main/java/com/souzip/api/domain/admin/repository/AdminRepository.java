package com.souzip.api.domain.admin.repository;

import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.Username;
import java.util.Optional;
import java.util.UUID;

public interface AdminRepository {

    Optional<Admin> findByUsername(Username username);

    Optional<Admin> findById(UUID id);

    Admin save(Admin admin);
}
