package com.souzip.application.admin;

import com.souzip.application.admin.provided.AdminFinder;
import com.souzip.application.admin.required.AdminRepository;
import com.souzip.domain.admin.Admin;
import com.souzip.domain.admin.exception.AdminNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AdminQueryService implements AdminFinder {

    private final AdminRepository adminRepository;

    @Override
    public Admin findById(UUID adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(AdminNotFoundException::new);
    }

    @Override
    public Page<Admin> findAll(Pageable pageable) {
        return adminRepository.findAll(pageable);
    }
}