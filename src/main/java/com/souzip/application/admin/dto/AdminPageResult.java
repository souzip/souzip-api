package com.souzip.application.admin.dto;

import com.souzip.domain.admin.Admin;

import java.util.List;

public record AdminPageResult(
        List<Admin> admins,
        long total,
        int totalPages
) {
}