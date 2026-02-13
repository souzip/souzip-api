package com.souzip.api.domain.admin.repository;

import com.souzip.api.domain.admin.model.Admin;
import com.souzip.api.domain.admin.model.Username;
import java.util.Optional;

public interface AdminRepository {

    Optional<Admin> findByUsername(Username username);

    Admin save(Admin admin);
}
