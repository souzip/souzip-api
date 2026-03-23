package com.souzip.domain.notification.dto;

import com.souzip.domain.notification.FcmToken;

public record FcmTokenResponse(
        Long id,
        String deviceId,
        boolean active
) {

    public static FcmTokenResponse from(FcmToken token) {
        return new FcmTokenResponse(token.getId(), token.getDeviceId(), token.isActive());
    }
}
