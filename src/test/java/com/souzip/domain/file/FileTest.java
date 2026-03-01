package com.souzip.domain.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class FileTest {

    @DisplayName("파일을 정상적으로 등록한다")
    @Test
    void register() {
        // given
        FileRegisterRequest request = createValidRequest();

        // when
        File file = File.register(request);

        // then
        assertThat(file.getEntityType()).isEqualTo("NOTICE");
        assertThat(file.getEntityId()).isEqualTo(1L);
        assertThat(file.getStorageKey()).isEqualTo("user123/uuid-1234.jpg");
        assertThat(file.getOriginalName()).isEqualTo("photo.jpg");
        assertThat(file.getFileSize()).isEqualTo(1024L);
        assertThat(file.getContentType()).isEqualTo("image/jpeg");
        assertThat(file.getDisplayOrder()).isEqualTo(1);
    }

    @DisplayName("필수 값이 null이면 예외가 발생한다")
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideNullFieldCases")
    void required_field_null(
            String description,
            FileRegisterRequest request,
            String expectedMessage
    ) {
        // when & then
        assertThatThrownBy(() -> File.register(request))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining(expectedMessage);
    }

    private static Stream<Arguments> provideNullFieldCases() {
        return Stream.of(
                arguments(
                        "엔티티 타입이 null",
                        FileRegisterRequest.of(null, 1L, "key", "name", 1024L, "type", 1),
                        "엔티티 타입은 필수입니다."
                ),
                arguments(
                        "엔티티 ID가 null",
                        FileRegisterRequest.of("NOTICE", null, "key", "name", 1024L, "type", 1),
                        "엔티티 ID는 필수입니다."
                ),
                arguments(
                        "스토리지 키가 null",
                        FileRegisterRequest.of("NOTICE", 1L, null, "name", 1024L, "type", 1),
                        "스토리지 키는 필수입니다."
                ),
                arguments(
                        "파일명이 null",
                        FileRegisterRequest.of("NOTICE", 1L, "key", null, 1024L, "type", 1),
                        "파일명은 필수입니다."
                ),
                arguments(
                        "파일 크기가 null",
                        FileRegisterRequest.of("NOTICE", 1L, "key", "name", null, "type", 1),
                        "파일 크기는 필수입니다."
                ),
                arguments(
                        "파일 타입이 null",
                        FileRegisterRequest.of("NOTICE", 1L, "key", "name", 1024L, null, 1),
                        "파일 타입은 필수입니다."
                ),
                arguments(
                        "정렬 순서가 null",
                        FileRegisterRequest.of("NOTICE", 1L, "key", "name", 1024L, "type", null),
                        "정렬 순서는 필수입니다."
                )
        );
    }

    private FileRegisterRequest createValidRequest() {
        return FileRegisterRequest.of(
                "NOTICE",
                1L,
                "user123/uuid-1234.jpg",
                "photo.jpg",
                1024L,
                "image/jpeg",
                1
        );
    }
}
