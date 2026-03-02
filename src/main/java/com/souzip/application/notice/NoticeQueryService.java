package com.souzip.application.notice;

import com.souzip.application.file.dto.FileResponse;
import com.souzip.application.file.provided.FileFinder;
import com.souzip.application.notice.dto.NoticeResponse;
import com.souzip.application.notice.provided.NoticeFinder;
import com.souzip.application.notice.required.NoticeRepository;
import com.souzip.domain.file.EntityType;
import com.souzip.domain.notice.Notice;
import com.souzip.domain.notice.NoticeStatus;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NoticeQueryService implements NoticeFinder {

    private final NoticeRepository noticeRepository;
    private final FileFinder fileFinder;

    @Override
    public Notice findById(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NoticeNotFoundException(noticeId));
    }

    @Override
    public List<Notice> findAllActive() {
        return noticeRepository.findByStatusOrderByCreatedAtDesc(NoticeStatus.ACTIVE);
    }

    @Override
    public List<Notice> findAll() {
        return noticeRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public NoticeResponse findByIdWithFiles(Long noticeId) {
        Notice notice = findById(noticeId);
        List<FileResponse> files = fileFinder.findFileResponsesByEntity(EntityType.NOTICE, noticeId);
        return NoticeResponse.from(notice, files);
    }

    @Override
    public NoticeResponse findActiveByIdWithFiles(Long noticeId) {
        Notice notice = findById(noticeId);

        if (!notice.isActive()) {
            throw new NoticeNotFoundException(noticeId);
        }

        List<FileResponse> files = fileFinder.findFileResponsesByEntity(EntityType.NOTICE, noticeId);
        return NoticeResponse.from(notice, files);
    }

    @Override
    public List<NoticeResponse> findAllActiveWithFiles() {
        return findNoticesWithFiles(findAllActive());
    }

    @Override
    public List<NoticeResponse> findAllWithFiles() {
        return findNoticesWithFiles(findAll());
    }

    private List<NoticeResponse> findNoticesWithFiles(List<Notice> notices) {
        if (notices.isEmpty()) {
            return List.of();
        }

        Map<Long, List<FileResponse>> filesMap = fetchFilesForNotices(notices);
        return combineNoticesWithFiles(notices, filesMap);
    }

    private Map<Long, List<FileResponse>> fetchFilesForNotices(List<Notice> notices) {
        List<Long> noticeIds = extractNoticeIds(notices);
        return fileFinder.findFilesByEntityIds(EntityType.NOTICE, noticeIds);
    }

    private List<Long> extractNoticeIds(List<Notice> notices) {
        return notices.stream()
                .map(Notice::getId)
                .toList();
    }

    private List<NoticeResponse> combineNoticesWithFiles(
            List<Notice> notices,
            Map<Long, List<FileResponse>> filesMap
    ) {
        return notices.stream()
                .map(notice -> NoticeResponse.from(notice, filesMap.getOrDefault(notice.getId(), List.of())))
                .toList();
    }
}
