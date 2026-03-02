package com.souzip.application.notice;

import com.souzip.application.file.dto.FileResponse;
import com.souzip.application.file.provided.FileFinder;
import com.souzip.application.notice.dto.NoticeResponse;
import com.souzip.application.notice.provided.NoticeFinder;
import com.souzip.application.notice.required.NoticeRepository;
import com.souzip.domain.notice.Notice;
import com.souzip.domain.notice.NoticeStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class NoticeQueryService implements NoticeFinder {

    public static final String ENTITY_TYPE_NOTICE = "NOTICE";

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

        List<FileResponse> files = fileFinder.findFileResponsesByEntity(ENTITY_TYPE_NOTICE, noticeId);

        return NoticeResponse.from(notice, files);
    }

    @Override
    public List<NoticeResponse> findAllActiveWithFiles() {
        return findAllActive().stream()
                .map(this::toResponseWithFiles)
                .toList();
    }

    @Override
    public List<NoticeResponse> findAllWithFiles() {
        return findAll().stream()
                .map(this::toResponseWithFiles)
                .toList();
    }

    private NoticeResponse toResponseWithFiles(Notice notice) {
        List<FileResponse> files = fileFinder.findFileResponsesByEntity(ENTITY_TYPE_NOTICE, notice.getId());

        return NoticeResponse.from(notice, files);
    }
}
