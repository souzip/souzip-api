package com.souzip.domain.notification;

import com.souzip.domain.shared.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken extends BaseEntity {

    private Long userId;

    private String token;

    private DeviceType deviceType;

    private String deviceId;

    private String deviceModel;

    private String osVersion;

    private String appVersion;

    private boolean active;

    private LocalDateTime lastUsedAt;

    public static FcmToken register(FcmTokenRegisterRequest request) {
        FcmToken fcmToken = new FcmToken();

        fcmToken.token = requireNonNull(request.fcmToken(), "FCM 토큰은 필수입니다.");
        fcmToken.deviceType = requireNonNull(request.deviceType(), "디바이스 타입은 필수입니다.");
        fcmToken.deviceId = requireNonNull(request.deviceId(), "디바이스 ID는 필수입니다.");
        fcmToken.deviceModel = request.deviceModel();
        fcmToken.osVersion = request.osVersion();
        fcmToken.appVersion = request.appVersion();
        fcmToken.active = true;
        fcmToken.lastUsedAt = LocalDateTime.now();

        return fcmToken;
    }

    public void linkUser(Long userId) {
        this.userId = requireNonNull(userId, "사용자 ID는 필수입니다.");
        this.lastUsedAt = LocalDateTime.now();
    }

    public void unlinkUser() {
        this.userId = null;
        this.lastUsedAt = LocalDateTime.now();
    }

    public void updateToken(String token) {
        this.token = requireNonNull(token, "FCM 토큰은 필수입니다.");
        this.lastUsedAt = LocalDateTime.now();
    }

    public void updateDeviceInfo(String deviceModel, String osVersion, String appVersion) {
        this.deviceModel = deviceModel;
        this.osVersion = osVersion;
        this.appVersion = appVersion;
        this.lastUsedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
        this.lastUsedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isLoggedIn() {
        return this.userId != null;
    }
}
