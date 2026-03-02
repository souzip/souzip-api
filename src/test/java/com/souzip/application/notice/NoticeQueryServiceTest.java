package com.souzip.application.notice;

import com.souzip.application.file.dto.FileResponse;
import com.souzip.application.file.provided.FileFinder;
import com.souzip.application.notice.dto.NoticeResponse;
import com.souzip.application.notice.required.NoticeRepository;
import com.souzip.domain.file.EntityType;
import com.souzip.domain.notice.Notice;
import com.souzip.domain.notice.NoticeRegisterRequest;
import com.souzip.domain.notice.NoticeStatus;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class NoticeQueryServiceTest {

    private static final UUID TEST_ADMIN_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private FileFinder fileFinder;

    @InjectMocks
    private NoticeQueryService noticeQueryService;

    @DisplayName("ID로 공지사항을 조회한다")
    @Test
    void findById() {
        Notice notice = createNotice(1L, "제목", "내용", NoticeStatus.ACTIVE);

        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));

        Notice result = noticeQueryService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("제목");
        assertThat(result.getContent()).isEqualTo("내용");
    }

    @DisplayName("존재하지 않는 공지사항 조회 시 예외가 발생한다")
    @Test
    void findById_notFound() {
        given(noticeRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> noticeQueryService.findById(1L))
                .isInstanceOf(NoticeNotFoundException.class)
                .hasMessageContaining("1");
    }

    @DisplayName("활성화된 공지사항 목록을 조회한다")
    @Test
    void findAllActive() {
        Notice notice1 = createNotice(1L, "제목1", "내용1", NoticeStatus.ACTIVE);
        Notice notice2 = createNotice(2L, "제목2", "내용2", NoticeStatus.ACTIVE);
        List<Notice> notices = List.of(notice1, notice2);

        given(noticeRepository.findByStatusOrderByCreatedAtDesc(NoticeStatus.ACTIVE))
                .willReturn(notices);

        List<Notice> result = noticeQueryService.findAllActive();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isEqualTo(NoticeStatus.ACTIVE);
        assertThat(result.get(1).getStatus()).isEqualTo(NoticeStatus.ACTIVE);
    }

    @DisplayName("전체 공지사항 목록을 조회한다")
    @Test
    void findAll() {
        Notice notice1 = createNotice(1L, "제목1", "내용1", NoticeStatus.ACTIVE);
        Notice notice2 = createNotice(2L, "제목2", "내용2", NoticeStatus.INACTIVE);
        List<Notice> notices = List.of(notice1, notice2);

        given(noticeRepository.findAllByOrderByCreatedAtDesc()).willReturn(notices);

        List<Notice> result = noticeQueryService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
    }

    @DisplayName("파일과 함께 공지사항을 조회한다")
    @Test
    void findByIdWithFiles() {
        Notice notice = createNotice(1L, "제목", "내용", NoticeStatus.ACTIVE);
        List<FileResponse> files = List.of(
                new FileResponse(1L, "https://example.com/file1.jpg", "file1.jpg", 1),
                new FileResponse(2L, "https://example.com/file2.jpg", "file2.jpg", 2)
        );

        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));
        given(fileFinder.findFileResponsesByEntity(eq(EntityType.NOTICE), eq(1L)))
                .willReturn(files);

        NoticeResponse result = noticeQueryService.findByIdWithFiles(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("제목");
        assertThat(result.files()).hasSize(2);
        assertThat(result.files().get(0).id()).isEqualTo(1L);
        assertThat(result.files().get(1).id()).isEqualTo(2L);
    }

    @DisplayName("파일이 없는 공지사항을 조회한다")
    @Test
    void findByIdWithFiles_noFiles() {
        Notice notice = createNotice(1L, "제목", "내용", NoticeStatus.ACTIVE);

        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));
        given(fileFinder.findFileResponsesByEntity(eq(EntityType.NOTICE), eq(1L)))
                .willReturn(List.of());

        NoticeResponse result = noticeQueryService.findByIdWithFiles(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.files()).isEmpty();
    }

    @DisplayName("활성 상태의 공지사항만 파일과 함께 조회한다")
    @Test
    void findActiveByIdWithFiles() {
        Notice notice = createNotice(1L, "제목", "내용", NoticeStatus.ACTIVE);
        List<FileResponse> files = List.of(
                new FileResponse(1L, "https://example.com/file1.jpg", "file1.jpg", 1)
        );

        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));
        given(fileFinder.findFileResponsesByEntity(eq(EntityType.NOTICE), eq(1L)))
                .willReturn(files);

        NoticeResponse result = noticeQueryService.findActiveByIdWithFiles(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("제목");
        assertThat(result.status()).isEqualTo(NoticeStatus.ACTIVE);
        assertThat(result.files()).hasSize(1);
    }

    @DisplayName("비활성 상태의 공지사항 조회 시 예외가 발생한다")
    @Test
    void findActiveByIdWithFiles_inactiveNotice() {
        Notice notice = createNotice(1L, "제목", "내용", NoticeStatus.INACTIVE);

        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));

        assertThatThrownBy(() -> noticeQueryService.findActiveByIdWithFiles(1L))
                .isInstanceOf(NoticeNotFoundException.class)
                .hasMessageContaining("1");
    }

    @DisplayName("파일과 함께 활성화된 공지사항 목록을 조회한다")
    @Test
    void findAllActiveWithFiles() {
        Notice notice1 = createNotice(1L, "제목1", "내용1", NoticeStatus.ACTIVE);
        Notice notice2 = createNotice(2L, "제목2", "내용2", NoticeStatus.ACTIVE);
        List<Notice> notices = List.of(notice1, notice2);

        List<FileResponse> files1 = List.of(
                new FileResponse(1L, "https://example.com/file1.jpg", "file1.jpg", 1)
        );
        Map<Long, List<FileResponse>> filesMap = Map.of(
                1L, files1,
                2L, List.of()
        );

        given(noticeRepository.findByStatusOrderByCreatedAtDesc(NoticeStatus.ACTIVE))
                .willReturn(notices);
        given(fileFinder.findFilesByEntityIds(eq(EntityType.NOTICE), eq(List.of(1L, 2L))))
                .willReturn(filesMap);

        List<NoticeResponse> result = noticeQueryService.findAllActiveWithFiles();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).files()).hasSize(1);
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).files()).isEmpty();
    }

    @DisplayName("파일과 함께 전체 공지사항 목록을 조회한다")
    @Test
    void findAllWithFiles() {
        Notice notice1 = createNotice(1L, "제목1", "내용1", NoticeStatus.ACTIVE);
        Notice notice2 = createNotice(2L, "제목2", "내용2", NoticeStatus.INACTIVE);
        List<Notice> notices = List.of(notice1, notice2);

        List<FileResponse> files1 = List.of(
                new FileResponse(1L, "https://example.com/file1.jpg", "file1.jpg", 1)
        );
        List<FileResponse> files2 = List.of(
                new FileResponse(2L, "https://example.com/file2.jpg", "file2.jpg", 1)
        );
        Map<Long, List<FileResponse>> filesMap = Map.of(
                1L, files1,
                2L, files2
        );

        given(noticeRepository.findAllByOrderByCreatedAtDesc()).willReturn(notices);
        given(fileFinder.findFilesByEntityIds(eq(EntityType.NOTICE), eq(List.of(1L, 2L))))
                .willReturn(filesMap);

        List<NoticeResponse> result = noticeQueryService.findAllWithFiles();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).files()).hasSize(1);
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).files()).hasSize(1);
    }

    @DisplayName("공지사항 목록이 비어있으면 빈 리스트를 반환한다")
    @Test
    void findAllActiveWithFiles_emptyList() {
        given(noticeRepository.findByStatusOrderByCreatedAtDesc(NoticeStatus.ACTIVE))
                .willReturn(List.of());

        List<NoticeResponse> result = noticeQueryService.findAllActiveWithFiles();

        assertThat(result).isEmpty();
    }

    private Notice createNotice(Long id, String title, String content, NoticeStatus status) {
        NoticeRegisterRequest request = NoticeRegisterRequest.of(
                title,
                content,
                TEST_ADMIN_ID,
                status
        );
        Notice notice = Notice.register(request);
        ReflectionTestUtils.setField(notice, "id", id);
        return notice;
    }
}
