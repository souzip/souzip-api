package com.souzip.api.domain.admin.model;

import com.souzip.api.domain.admin.exception.InvalidUsernameException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UsernameTest {

    @Test
    @DisplayName("유효한 아이디로 Username 생성에 성공한다.")
    void create_success() {
        Username username = new Username("admin123");

        assertThat(username.value()).isEqualTo("admin123");
    }

    @Test
    @DisplayName("아이디가 null이면 예외가 발생한다.")
    void create_fail_null() {
        assertThatThrownBy(() -> new Username(null))
            .isInstanceOf(InvalidUsernameException.class);
    }

    @Test
    @DisplayName("아이디가 공백이면 예외가 발생한다.")
    void create_fail_blank() {
        assertThatThrownBy(() -> new Username("   "))
            .isInstanceOf(InvalidUsernameException.class);
    }

    @ParameterizedTest
    @DisplayName("아이디가 길이 범위를 벗어나면 예외가 발생한다.")
    @ValueSource(strings = {"a", "ab", "abc", "12345678901", "123456789012"})
    void create_fail_invalid_length(String value) {
        assertThatThrownBy(() -> new Username(value))
            .isInstanceOf(InvalidUsernameException.class);
    }
}
