package com.souzip.domain.admin;

public interface PasswordEncoder {

    String encode(String password);

    boolean matches(String password, String encodedPassword);
}
