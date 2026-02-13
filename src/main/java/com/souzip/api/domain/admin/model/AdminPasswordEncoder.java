package com.souzip.api.domain.admin.model;

public interface AdminPasswordEncoder {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
