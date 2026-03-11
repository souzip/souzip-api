package com.souzip.application.admin.provided;

import com.souzip.domain.admin.Admin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminFinder {
    Admin findById(UUID adminId);

    Page<Admin> findAll(Pageable pageable);
}