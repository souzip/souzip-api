package com.souzip.api.domain.admin.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordTest {

    @Test
    @DisplayName("비밀번호 인코딩에 성공한다.")
    void encode_success() {
        AdminPasswordEncoder encoder = rawPassword -> "encoded_" + rawPassword;

        Password password = Password.encode("password123", encoder);

        assertThat(password).isNotNull();
    }
}
