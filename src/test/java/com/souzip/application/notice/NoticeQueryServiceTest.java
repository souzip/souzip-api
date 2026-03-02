package com.souzip.application.notice;

import com.souzip.application.notice.required.NoticeRepository;
import com.souzip.domain.notice.Notice;
import com.souzip.domain.notice.NoticeRegisterRequest;
import com.souzip.domain.notice.NoticeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NoticeQueryServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @InjectMocks
    private NoticeQueryService noticeQueryService;

    @DisplayName("ID로 공지사항을 조회한다")
    @Test
    void findById() {
        // given
        Notice notice = createNotice(1L, "제목", "내용", NoticeStatus.ACTIVE);

        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));

        // when
        Notice result = noticeQueryService.findById(1L);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("제목");
        assertThat(result.getContent()).isEqualTo("내용");
    }

    @DisplayName("존재하지 않는 공지사항 조회 시 예외가 발생한다")
    @Test
    void findById_notFound() {
        // given
        given(noticeRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> noticeQueryService.findById(1L))
                .isInstanceOf(NoticeNotFoundException.class)
                .hasMessageContaining("1");
    }

    @DisplayName("활성화된 공지사항 목록을 조회한다")
    @Test
    void findAllActive() {
        // given
        Notice notice1 = createNotice(1L, "제목1", "내용1", NoticeStatus.ACTIVE);
        Notice notice2 = createNotice(2L, "제목2", "내용2", NoticeStatus.ACTIVE);
        List<Notice> notices = List.of(notice1, notice2);

        given(noticeRepository.findByStatusOrderByCreatedAtDesc(NoticeStatus.ACTIVE))
                .willReturn(notices);

        // when
        List<Notice> result = noticeQueryService.findAllActive();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isEqualTo(NoticeStatus.ACTIVE);
        assertThat(result.get(1).getStatus()).isEqualTo(NoticeStatus.ACTIVE);
    }

    @DisplayName("전체 공지사항 목록을 조회한다")
    @Test
    void findAll() {
        // given
        Notice notice1 = createNotice(1L, "제목1", "내용1", NoticeStatus.ACTIVE);
        Notice notice2 = createNotice(2L, "제목2", "내용2", NoticeStatus.INACTIVE);
        List<Notice> notices = List.of(notice1, notice2);

        given(noticeRepository.findAllByOrderByCreatedAtDesc()).willReturn(notices);

        // when
        List<Notice> result = noticeQueryService.findAll();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    private Notice createNotice(Long id, String title, String content, NoticeStatus status) {
        NoticeRegisterRequest request = NoticeRegisterRequest.of(
                title,
                content,
                1L,
                status
        );
        Notice notice = Notice.register(request);
        ReflectionTestUtils.setField(notice, "id", id);
        return notice;
    }
}
