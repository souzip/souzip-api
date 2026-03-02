package com.souzip.application.notice.required;

import com.souzip.domain.notice.Notice;
import com.souzip.domain.notice.NoticeStatus;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends Repository<Notice, Long> {

    Notice save(Notice notice);

    Optional<Notice> findById(Long id);

    List<Notice> findAllByOrderByCreatedAtDesc();

    List<Notice> findByStatusOrderByCreatedAtDesc(NoticeStatus status);

    void delete(Notice notice);
}
