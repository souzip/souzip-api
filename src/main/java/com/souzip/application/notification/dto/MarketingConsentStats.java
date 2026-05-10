package com.souzip.application.notification.dto;

public record MarketingConsentStats(
        long totalUsers,
        long consentedUsers,
        long declinedUsers
) {
}
