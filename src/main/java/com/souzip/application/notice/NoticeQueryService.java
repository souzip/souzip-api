package com.souzip.application.notice;

import com.souzip.application.notice.dto.NoticeResponse;
import com.souzip.application.notice.provided.NoticeFinder;
import com.souzip.application.notice.required.NoticeRepository;
import com.souzip.application.notice.assembler.NoticeResponseAssembler;
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
    private final NoticeResponseAssembler assembler;

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

        return assembler.assemble(notice);
    }

    @Override
    public NoticeResponse findActiveByIdWithFiles(Long noticeId) {
        Notice notice = findById(noticeId);

        if (!notice.isActive()) {
            throw new NoticeNotFoundException(noticeId);
        }

        return assembler.assemble(notice);
    }

    @Override
    public List<NoticeResponse> findAllActiveWithFiles() {
        return assembler.assembleAll(findAllActive());
    }

    @Override
    public List<NoticeResponse> findAllWithFiles() {
        return assembler.assembleAll(findAll());
    }
}
