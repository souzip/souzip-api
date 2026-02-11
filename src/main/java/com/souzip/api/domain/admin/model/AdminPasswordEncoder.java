package com.souzip.api.domain.admin.model;

public interface AdminPasswordEncoder {
    String encode(String rawPassword);
}
