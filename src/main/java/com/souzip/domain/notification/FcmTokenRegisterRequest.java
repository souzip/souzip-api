package com.souzip.domain.notification;

import static java.util.Objects.requireNonNull;

public record FcmTokenRegisterRequest(
        String fcmToken,
        DeviceType deviceType,
        String deviceId,
        String deviceModel,
        String osVersion,
        String appVersion
) {
    private static final int MAX_FCM_TOKEN_LENGTH = 255;
    private static final int MAX_DEVICE_ID_LENGTH = 100;
    private static final int MAX_DEVICE_MODEL_LENGTH = 100;
    private static final int MAX_OS_VERSION_LENGTH = 50;
    private static final int MAX_APP_VERSION_LENGTH = 50;

    public FcmTokenRegisterRequest {
        fcmToken = validateFcmToken(fcmToken);
        requireNonNull(deviceType, "디바이스 타입은 필수입니다.");
        deviceId = validateDeviceId(deviceId);
        deviceModel = validateOptionalField(deviceModel, MAX_DEVICE_MODEL_LENGTH, "디바이스 모델");
        osVersion = validateOptionalField(osVersion, MAX_OS_VERSION_LENGTH, "OS 버전");
        appVersion = validateOptionalField(appVersion, MAX_APP_VERSION_LENGTH, "앱 버전");
    }

    public static FcmTokenRegisterRequest of(
            String fcmToken,
            DeviceType deviceType,
            String deviceId,
            String deviceModel,
            String osVersion,
            String appVersion
    ) {
        return new FcmTokenRegisterRequest(
                fcmToken,
                deviceType,
                deviceId,
                deviceModel,
                osVersion,
                appVersion
        );
    }

    private static String validateFcmToken(String fcmToken) {
        requireNonNull(fcmToken, "FCM 토큰은 필수입니다.");

        String trimmed = fcmToken.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("FCM 토큰은 비어있을 수 없습니다.");
        }

        if (trimmed.length() > MAX_FCM_TOKEN_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("FCM 토큰은 %d자를 초과할 수 없습니다.", MAX_FCM_TOKEN_LENGTH)
            );
        }

        return trimmed;
    }

    private static String validateDeviceId(String deviceId) {
        requireNonNull(deviceId, "디바이스 ID는 필수입니다.");

        String trimmed = deviceId.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("디바이스 ID는 비어있을 수 없습니다.");
        }

        if (trimmed.length() > MAX_DEVICE_ID_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("디바이스 ID는 %d자를 초과할 수 없습니다.", MAX_DEVICE_ID_LENGTH)
            );
        }

        return trimmed;
    }

    private static String validateOptionalField(String value, int maxLength, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(
                    String.format("%s는 %d자를 초과할 수 없습니다.", fieldName, maxLength)
            );
        }

        return trimmed;
    }
}
