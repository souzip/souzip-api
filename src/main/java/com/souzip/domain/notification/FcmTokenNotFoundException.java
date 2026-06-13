package com.souzip.domain.notification;

public class FcmTokenNotFoundException extends RuntimeException {

    public FcmTokenNotFoundException(String message) {
        super(message);
    }

    // 토큰·디바이스 ID 등 식별자는 민감 정보이므로 메시지에 포함하지 않습니다.
    public static FcmTokenNotFoundException byDeviceId(String deviceId) {
        return new FcmTokenNotFoundException("해당 디바이스에 등록된 FCM 토큰을 찾을 수 없습니다.");
    }

    public static FcmTokenNotFoundException byToken(String token) {
        return new FcmTokenNotFoundException("FCM 토큰을 찾을 수 없습니다.");
    }
}
