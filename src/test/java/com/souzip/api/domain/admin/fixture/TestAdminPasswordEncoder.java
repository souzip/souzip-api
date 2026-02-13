package com.souzip.api.domain.admin.fixture;

import com.souzip.api.domain.admin.model.AdminPasswordEncoder;

public class TestAdminPasswordEncoder implements AdminPasswordEncoder {

    @Override
    public String encode(String rawPassword) {
        return "encoded_" + rawPassword;
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return encodedPassword.equals("encoded_" + rawPassword);
    }
}
