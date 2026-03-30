package com.souzip.application.email.dto;

public record EmailBroadcastResult(
        int totalTargets,
        int successCount,
        int failCount,
        boolean smtpConfigured
) {
}
