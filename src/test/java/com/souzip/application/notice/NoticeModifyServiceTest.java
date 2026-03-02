package com.souzip.application.notice;

import com.souzip.application.file.provided.FileModifier;
import com.souzip.application.notice.provided.NoticeFinder;
import com.souzip.application.notice.required.NoticeRepository;
import com.souzip.domain.notice.Notice;
import com.souzip.domain.notice.NoticeRegisterRequest;
import com.souzip.domain.notice.NoticeStatus;
import com.souzip.domain.notice.NoticeUpdateRequest;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class NoticeModifyServiceTest {

    private static final UUID TEST_ADMIN_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private NoticeFinder noticeFinder;

    @Mock
    private FileModifier fileModifier;

    @InjectMocks
    private NoticeModifyService noticeModifyService;

    @DisplayName("공지사항을 등록한다")
    @Test
    void register() {
        // given
        NoticeRegisterRequest request = NoticeRegisterRequest.of(
                "제목",
                "내용",
                TEST_ADMIN_ID,
                NoticeStatus.ACTIVE
        );

        MultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        given(noticeRepository.save(any(Notice.class)))
                .willAnswer(invocation -> {
                    Notice notice = invocation.getArgument(0);
                    ReflectionTestUtils.setField(notice, "id", 1L);
                    return notice;
                });

        // when
        Notice result = noticeModifyService.register(request, List.of(file));

        // then
        assertThat(result.getTitle()).isEqualTo("제목");
        assertThat(result.getContent()).isEqualTo("내용");
        assertThat(result.getStatus()).isEqualTo(NoticeStatus.ACTIVE);

        then(fileModifier).should(times(1))
                .register(
                        eq(TEST_ADMIN_ID.toString()),
                        eq("NOTICE"),
                        eq(1L),
                        eq(file),
                        eq(null)
                );
    }

    @DisplayName("파일 없이 공지사항을 등록한다")
    @Test
    void registerWithoutFiles() {
        // given
        NoticeRegisterRequest request = NoticeRegisterRequest.of(
                "제목",
                "내용",
                TEST_ADMIN_ID,
                NoticeStatus.ACTIVE
        );

        given(noticeRepository.save(any(Notice.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        Notice result = noticeModifyService.register(request, null);

        // then
        assertThat(result.getTitle()).isEqualTo("제목");
        then(fileModifier).shouldHaveNoInteractions();
    }

    @DisplayName("공지사항을 수정한다")
    @Test
    void update() {
        // given
        Notice notice = createNotice(NoticeStatus.INACTIVE);
        NoticeUpdateRequest request = NoticeUpdateRequest.of(
                "수정된 제목",
                "수정된 내용",
                NoticeStatus.ACTIVE
        );

        given(noticeFinder.findById(1L)).willReturn(notice);
        given(noticeRepository.save(any(Notice.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        Notice result = noticeModifyService.update(1L, request, null, null);

        // then
        assertThat(result.getTitle()).isEqualTo("수정된 제목");
        assertThat(result.getContent()).isEqualTo("수정된 내용");
        assertThat(result.getStatus()).isEqualTo(NoticeStatus.ACTIVE);
    }

    @DisplayName("공지사항 수정 시 파일을 삭제하고 추가한다")
    @Test
    void updateWithFiles() {
        // given
        Notice notice = createNotice(NoticeStatus.ACTIVE);
        NoticeUpdateRequest request = NoticeUpdateRequest.of(
                "수정된 제목",
                "수정된 내용",
                NoticeStatus.ACTIVE
        );

        List<Long> deleteFileIds = List.of(10L, 20L);
        MultipartFile newFile = new MockMultipartFile(
                "file",
                "new.jpg",
                "image/jpeg",
                "new".getBytes()
        );

        given(noticeFinder.findById(1L)).willReturn(notice);
        given(noticeRepository.save(any(Notice.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        noticeModifyService.update(1L, request, deleteFileIds, List.of(newFile));

        // then
        then(fileModifier).should(times(1)).delete(10L);
        then(fileModifier).should(times(1)).delete(20L);
        then(fileModifier).should(times(1))
                .register(
                        eq(TEST_ADMIN_ID.toString()),
                        eq("NOTICE"),
                        eq(1L),
                        eq(newFile),
                        eq(null)
                );
    }

    @DisplayName("공지사항을 활성화한다")
    @Test
    void activate() {
        // given
        Notice notice = createNotice(NoticeStatus.INACTIVE);

        given(noticeFinder.findById(1L)).willReturn(notice);
        given(noticeRepository.save(any(Notice.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        noticeModifyService.activate(1L);

        // then
        assertThat(notice.getStatus()).isEqualTo(NoticeStatus.ACTIVE);
        assertThat(notice.isActive()).isTrue();
    }

    @DisplayName("공지사항을 비활성화한다")
    @Test
    void deactivate() {
        // given
        Notice notice = createNotice(NoticeStatus.ACTIVE);

        given(noticeFinder.findById(1L)).willReturn(notice);
        given(noticeRepository.save(any(Notice.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        noticeModifyService.deactivate(1L);

        // then
        assertThat(notice.getStatus()).isEqualTo(NoticeStatus.INACTIVE);
        assertThat(notice.isActive()).isFalse();
    }

    @DisplayName("공지사항을 삭제한다")
    @Test
    void delete() {
        // given
        Notice notice = createNotice(NoticeStatus.ACTIVE);

        given(noticeFinder.findById(1L)).willReturn(notice);

        // when
        noticeModifyService.delete(1L);

        // then
        then(fileModifier).should(times(1)).deleteByEntity("NOTICE", 1L);
        then(noticeRepository).should(times(1)).delete(notice);
    }

    private Notice createNotice(NoticeStatus status) {
        NoticeRegisterRequest request = NoticeRegisterRequest.of(
                "제목",
                "내용",
                TEST_ADMIN_ID,
                status
        );
        Notice notice = Notice.register(request);
        ReflectionTestUtils.setField(notice, "id", 1L);
        return notice;
    }
}
