package com.souzip.application.notification.dto;

import com.souzip.domain.notification.PushBroadcastHistory;
import java.time.LocalDateTime;
import java.util.UUID;

public record PushBroadcastHistoryResponse(
        Long id,
        UUID adminId,
        String title,
        String body,
        int totalTargets,
        int successCount,
        int failCount,
        boolean firebaseConfigured,
        LocalDateTime createdAt
) {

    public static PushBroadcastHistoryResponse from(PushBroadcastHistory entity) {
        return new PushBroadcastHistoryResponse(
                entity.getId(),
                entity.getAdminId(),
                entity.getTitle(),
                entity.getBody(),
                entity.getTotalTargets(),
                entity.getSuccessCount(),
                entity.getFailCount(),
                entity.isFirebaseConfigured(),
                entity.getCreatedAt()
        );
    }
}
