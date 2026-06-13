package com.souzip.application.notification.dto;

public record PushBroadcastResult(
        int totalTargets,
        int successCount,
        int failCount,
        boolean firebaseConfigured
) {
}
