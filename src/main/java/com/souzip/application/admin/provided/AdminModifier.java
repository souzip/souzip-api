package com.souzip.application.admin.provided;

import com.souzip.domain.admin.Admin;
import com.souzip.domain.admin.AdminRegisterRequest;

import java.util.UUID;

public interface AdminModifier {
    Admin register(AdminRegisterRequest request);

    void delete(UUID adminId, UUID requesterId);
}