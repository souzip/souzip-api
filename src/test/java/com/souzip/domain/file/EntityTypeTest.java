package com.souzip.domain.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityTypeTest {

    @DisplayName("문자열로부터 EntityType을 정상적으로 변환한다")
    @ParameterizedTest
    @CsvSource({
            "Notice, NOTICE",
            "Souvenir, SOUVENIR"
    })
    void from_success(String value, EntityType expected) {
        // when
        EntityType result = EntityType.from(value);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @DisplayName("유효하지 않은 문자열은 예외가 발생한다")
    @ParameterizedTest
    @CsvSource({
            "INVALID",
            "notice",
            "NOTICE",
            "Unknown",
            "''"
    })
    void from_invalid(String value) {
        // when & then
        assertThatThrownBy(() -> EntityType.from(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown EntityType");
    }

    @DisplayName("getValue()는 저장된 문자열을 반환한다")
    @Test
    void getValue() {
        // when & then
        assertThat(EntityType.NOTICE.getValue()).isEqualTo("Notice");
        assertThat(EntityType.SOUVENIR.getValue()).isEqualTo("Souvenir");
    }

    @DisplayName("모든 EntityType이 정의되어 있다")
    @Test
    void all_types_defined() {
        // when
        EntityType[] values = EntityType.values();

        // then
        assertThat(values).hasSize(2);
        assertThat(values).containsExactly(EntityType.NOTICE, EntityType.SOUVENIR);
    }
}
