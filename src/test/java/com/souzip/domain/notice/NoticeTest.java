package com.souzip.domain.notice;

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

    @DisplayName("공지사항을 정상적으로 등록한다")
    @Test
    void register() {
        // given
        NoticeRegisterRequest request = NoticeRegisterRequest.of(
                "공지사항 제목",
                "공지사항 내용",
                1L,
                NoticeStatus.ACTIVE
        );

        // when
        Notice notice = Notice.register(request);

        // then
        assertThat(notice.getTitle()).isEqualTo("공지사항 제목");
        assertThat(notice.getContent()).isEqualTo("공지사항 내용");
        assertThat(notice.getAuthorId()).isEqualTo(1L);
        assertThat(notice.getStatus()).isEqualTo(NoticeStatus.ACTIVE);
    }

    @DisplayName("필수 값이 null이면 예외가 발생한다")
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideNullFieldCases")
    void register_fail_required_field_null(
            String description,
            NoticeRegisterRequest request,
            String expectedMessage
    ) {
        // when & then
        assertThatThrownBy(() -> Notice.register(request))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining(expectedMessage);
    }

    private static Stream<Arguments> provideNullFieldCases() {
        return Stream.of(
                arguments(
                        "제목이 null",
                        NoticeRegisterRequest.of(null, "내용", 1L, NoticeStatus.ACTIVE),
                        "제목은 필수입니다."
                ),
                arguments(
                        "내용이 null",
                        NoticeRegisterRequest.of("제목", null, 1L, NoticeStatus.ACTIVE),
                        "내용은 필수입니다."
                ),
                arguments(
                        "작성자 ID가 null",
                        NoticeRegisterRequest.of("제목", "내용", null, NoticeStatus.ACTIVE),
                        "작성자는 필수입니다."
                ),
                arguments(
                        "상태가 null",
                        NoticeRegisterRequest.of("제목", "내용", 1L, null),
                        "상태는 필수입니다."
                )
        );
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
                1L,
                NoticeStatus.INACTIVE
        );
        return Notice.register(request);
    }
}
