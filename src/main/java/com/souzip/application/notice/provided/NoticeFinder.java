package com.souzip.application.notice.provided;

import com.souzip.application.notice.dto.NoticeResponse;
import com.souzip.domain.notice.Notice;
import java.util.List;

public interface NoticeFinder {

    Notice findById(Long noticeId);

    List<Notice> findAllActive();

    List<Notice> findAll();

    NoticeResponse findByIdWithFiles(Long noticeId);

    NoticeResponse findActiveByIdWithFiles(Long noticeId);

    List<NoticeResponse> findAllActiveWithFiles();

    List<NoticeResponse> findAllWithFiles();
}
