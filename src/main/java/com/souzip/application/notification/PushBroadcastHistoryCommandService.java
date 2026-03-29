package com.souzip.application.notification;

import com.souzip.application.notification.dto.PushBroadcastResult;
import com.souzip.application.notification.required.PushBroadcastHistoryRepository;
import com.souzip.domain.notification.PushBroadcastHistory;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PushBroadcastHistoryCommandService {

    private final PushBroadcastHistoryRepository pushBroadcastHistoryRepository;

    // 관리자 푸시 브로드캐스트 결과를 이력으로 저장합니다.
    @Transactional
    public void record(UUID adminId, String title, String body, PushBroadcastResult result) {
        PushBroadcastHistory row = PushBroadcastHistory.record(
                adminId,
                title,
                body,
                result.totalTargets(),
                result.successCount(),
                result.failCount(),
                result.firebaseConfigured()
        );
        pushBroadcastHistoryRepository.save(row);
    }
}
