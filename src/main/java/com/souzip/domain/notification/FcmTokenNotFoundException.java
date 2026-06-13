package com.souzip.domain.notification;

public class FcmTokenNotFoundException extends RuntimeException {

    public FcmTokenNotFoundException(String message) {
        super(message);
    }

    public static FcmTokenNotFoundException byDeviceId(String deviceId) {
        return new FcmTokenNotFoundException(
                String.format("디바이스 ID '%s'에 해당하는 FCM 토큰을 찾을 수 없습니다.", deviceId)
        );
    }

    public static FcmTokenNotFoundException byToken(String token) {
        return new FcmTokenNotFoundException(
                String.format("FCM 토큰 '%s'를 찾을 수 없습니다.", token)
        );
    }
}
