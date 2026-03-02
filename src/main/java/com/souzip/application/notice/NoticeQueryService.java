package com.souzip.application.notice;

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

    private final NoticeRepository noticeRepository;

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
}
