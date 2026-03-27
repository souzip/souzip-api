package com.souzip.application.notification.required;

import com.souzip.domain.notification.PushBroadcastHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

public interface PushBroadcastHistoryRepository extends Repository<PushBroadcastHistory, Long> {

    PushBroadcastHistory save(PushBroadcastHistory entity);

    Page<PushBroadcastHistory> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
