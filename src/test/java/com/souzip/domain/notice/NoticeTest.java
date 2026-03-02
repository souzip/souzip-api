package com.souzip.domain.notice;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class NoticeTest {

    private static final UUID TEST_ADMIN_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @DisplayName("공지사항을 정상적으로 등록한다")
    @Test
    void register() {
        // given
        NoticeRegisterRequest request = NoticeRegisterRequest.of(
                "공지사항 제목",
                "공지사항 내용",
                TEST_ADMIN_ID,
                NoticeStatus.ACTIVE
        );

        // when
        Notice notice = Notice.register(request);

        // then
        assertThat(notice.getTitle()).isEqualTo("공지사항 제목");
        assertThat(notice.getContent()).isEqualTo("공지사항 내용");
        assertThat(notice.getAuthorId()).isEqualTo(TEST_ADMIN_ID);
        assertThat(notice.getStatus()).isEqualTo(NoticeStatus.ACTIVE);
    }

    @DisplayName("공지사항을 수정한다")
    @Test
    void update() {
        // given
        Notice notice = createNotice();
        NoticeUpdateRequest request = NoticeUpdateRequest.of(
                "수정된 제목",
                "수정된 내용",
                NoticeStatus.ACTIVE
        );

        // when
        notice.update(request);

        // then
        assertThat(notice.getTitle()).isEqualTo("수정된 제목");
        assertThat(notice.getContent()).isEqualTo("수정된 내용");
        assertThat(notice.getStatus()).isEqualTo(NoticeStatus.ACTIVE);
    }

    @DisplayName("공지사항을 활성화한다")
    @Test
    void activate() {
        // given
        Notice notice = createNotice();

        // when
        notice.activate();

        // then
        assertThat(notice.getStatus()).isEqualTo(NoticeStatus.ACTIVE);
        assertThat(notice.isActive()).isTrue();
    }

    @DisplayName("공지사항을 비활성화한다")
    @Test
    void deactivate() {
        // given
        Notice notice = createNotice();
        notice.activate();

        // when
        notice.deactivate();

        // then
        assertThat(notice.getStatus()).isEqualTo(NoticeStatus.INACTIVE);
        assertThat(notice.isActive()).isFalse();
    }

    private Notice createNotice() {
        NoticeRegisterRequest request = NoticeRegisterRequest.of(
                "공지사항 제목",
                "공지사항 내용",
                TEST_ADMIN_ID,
                NoticeStatus.INACTIVE
        );
        return Notice.register(request);
    }
}
