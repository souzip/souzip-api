package com.souzip.domain.admin.fixture;

import com.souzip.domain.admin.model.AdminPasswordEncoder;

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
