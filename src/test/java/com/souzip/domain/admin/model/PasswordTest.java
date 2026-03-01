package com.souzip.domain.admin.model;

import com.souzip.domain.admin.fixture.TestAdminPasswordEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordTest {

    @DisplayName("비밀번호 인코딩에 성공한다.")
    @Test
    void encode_success() {
        // given
        AdminPasswordEncoder encoder = new TestAdminPasswordEncoder();

        // when
        Password password = Password.encode("password123", encoder);

        // then
        assertThat(password).isNotNull();
    }
}
