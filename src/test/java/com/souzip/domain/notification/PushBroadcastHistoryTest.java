package com.souzip.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PushBroadcastHistoryTest {

    private static final UUID ADMIN_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @DisplayName("브로드캐스트 이력을 정상적으로 기록한다")
    @Test
    void record_success() {
        // when
        PushBroadcastHistory history = PushBroadcastHistory.record(
                ADMIN_ID, "제목", "본문", 100, 95, 5, true
        );

        // then
        assertThat(history.getAdminId()).isEqualTo(ADMIN_ID);
        assertThat(history.getTitle()).isEqualTo("제목");
        assertThat(history.getBody()).isEqualTo("본문");
        assertThat(history.getTotalTargets()).isEqualTo(100);
        assertThat(history.getSuccessCount()).isEqualTo(95);
        assertThat(history.getFailCount()).isEqualTo(5);
        assertThat(history.isFirebaseConfigured()).isTrue();
    }

    @DisplayName("Firebase 미설정 상태도 기록된다")
    @Test
    void record_withFirebaseDisabled() {
        // when
        PushBroadcastHistory history = PushBroadcastHistory.record(
                ADMIN_ID, "제목", "본문", 50, 0, 0, false
        );

        // then
        assertThat(history.isFirebaseConfigured()).isFalse();
        assertThat(history.getTotalTargets()).isEqualTo(50);
        assertThat(history.getSuccessCount()).isZero();
    }

    @DisplayName("관리자 ID가 null이면 예외가 발생한다")
    @Test
    void record_withNullAdminId_throwsException() {
        assertThatThrownBy(() ->
                PushBroadcastHistory.record(null, "제목", "본문", 0, 0, 0, true)
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("관리자 ID는 필수입니다.");
    }

    @DisplayName("제목이 null이면 예외가 발생한다")
    @Test
    void record_withNullTitle_throwsException() {
        assertThatThrownBy(() ->
                PushBroadcastHistory.record(ADMIN_ID, null, "본문", 0, 0, 0, true)
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("제목은 필수입니다.");
    }

    @DisplayName("본문이 null이면 예외가 발생한다")
    @Test
    void record_withNullBody_throwsException() {
        assertThatThrownBy(() ->
                PushBroadcastHistory.record(ADMIN_ID, "제목", null, 0, 0, 0, true)
        ).isInstanceOf(NullPointerException.class)
                .hasMessage("본문은 필수입니다.");
    }
}
