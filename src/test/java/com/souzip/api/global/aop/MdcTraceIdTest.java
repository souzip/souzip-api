package com.souzip.api.global.aop;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MdcTraceIdTest {

    @AfterEach
    void tearDown() {
        MdcTraceId.remove();
    }

    @DisplayName("TraceId 생성")
    @Test
    void generate() {
        // when
        String traceId = MdcTraceId.generate();

        // then
        assertThat(traceId).hasSize(8);
        assertThat(traceId).isNotNull();
    }

    @DisplayName("TraceId 저장 및 조회")
    @Test
    void putAndGet() {
        // given
        String traceId = "test-trace-id";

        // when
        MdcTraceId.put(traceId);
        String retrievedTraceId = MdcTraceId.get();

        // then
        assertThat(retrievedTraceId).isEqualTo(traceId);
    }

    @DisplayName("TraceId 존재 여부 확인")
    @Test
    void isPresent() {
        // when & then
        assertThat(MdcTraceId.isPresent()).isFalse();

        MdcTraceId.put("test-trace-id");
        assertThat(MdcTraceId.isPresent()).isTrue();

        MdcTraceId.remove();
        assertThat(MdcTraceId.isPresent()).isFalse();
    }

    @DisplayName("TraceId가 없을 때 생성하고 true 반환")
    @Test
    void putIfAbsentWhenPresent() {
        // when
        boolean isNewTrace = MdcTraceId.putIfAbsent();

        // then
        assertThat(isNewTrace).isTrue();
        assertThat(MdcTraceId.isPresent()).isTrue();
        assertThat(MdcTraceId.get()).isNotNull();
    }

    @DisplayName("TraceId가 있을 때 생성하지 않고 false 반환")
    @Test
    void putIfAbsentWhenNotPresent() {
        // given
        String existingTraceId = "existing-trace-id";
        MdcTraceId.put(existingTraceId);

        // when
        boolean isNewTrace = MdcTraceId.putIfAbsent();

        // then
        assertThat(isNewTrace).isFalse();
        assertThat(MdcTraceId.get()).isEqualTo(existingTraceId);
    }

    @DisplayName("TraceId 제거")
    @Test
    void remove() {
        // given
        MdcTraceId.put("test-trace-id");

        // when
        MdcTraceId.remove();

        // then
        assertThat(MdcTraceId.isPresent()).isFalse();
        assertThat(MdcTraceId.get()).isNull();
    }
}
