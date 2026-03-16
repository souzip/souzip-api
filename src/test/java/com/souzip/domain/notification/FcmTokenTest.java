package com.souzip.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class FcmTokenTest {

    @DisplayName("FCM 토큰을 정상적으로 등록한다")
    @Test
    void register_success() {
        // given
        FcmTokenRegisterRequest request = FcmTokenRegisterRequest.of(
                "fcm-token-123",
                DeviceType.ANDROID,
                "device-id-123",
                "Galaxy S23",
                "Android 14",
                "1.0.0"
        );

        // when
        FcmToken fcmToken = FcmToken.register(request);

        // then
        assertThat(fcmToken.getToken()).isEqualTo("fcm-token-123");
        assertThat(fcmToken.getDeviceType()).isEqualTo(DeviceType.ANDROID);
        assertThat(fcmToken.getDeviceId()).isEqualTo("device-id-123");
        assertThat(fcmToken.getDeviceModel()).isEqualTo("Galaxy S23");
        assertThat(fcmToken.getOsVersion()).isEqualTo("Android 14");
        assertThat(fcmToken.getAppVersion()).isEqualTo("1.0.0");
        assertThat(fcmToken.isActive()).isTrue();
        assertThat(fcmToken.getUserId()).isNull();
        assertThat(fcmToken.getLastUsedAt()).isNotNull();
    }

    @DisplayName("선택 필드 없이 FCM 토큰을 등록한다")
    @Test
    void register_withoutOptionalFields() {
        // given
        FcmTokenRegisterRequest request = FcmTokenRegisterRequest.of(
                "fcm-token-123",
                DeviceType.IOS,
                "device-id-456",
                null,
                null,
                null
        );

        // when
        FcmToken fcmToken = FcmToken.register(request);

        // then
        assertThat(fcmToken.getToken()).isEqualTo("fcm-token-123");
        assertThat(fcmToken.getDeviceType()).isEqualTo(DeviceType.IOS);
        assertThat(fcmToken.getDeviceModel()).isNull();
        assertThat(fcmToken.getOsVersion()).isNull();
        assertThat(fcmToken.getAppVersion()).isNull();
    }

    @DisplayName("로그인 시 사용자 ID를 연결한다")
    @Test
    void linkUser_success() {
        // given
        FcmToken fcmToken = createFcmToken();
        Long userId = 1L;

        // when
        fcmToken.linkUser(userId);

        // then
        assertThat(fcmToken.getUserId()).isEqualTo(userId);
        assertThat(fcmToken.isLoggedIn()).isTrue();
        assertThat(fcmToken.getLastUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("null 사용자 ID로 연결하면 예외가 발생한다")
    void linkUser_withNull_throwsException() {
        // given
        FcmToken fcmToken = createFcmToken();

        // when & then
        assertThatThrownBy(() -> fcmToken.linkUser(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("사용자 ID는 필수입니다.");
    }

    @DisplayName("로그아웃 시 사용자 ID 연결을 해제한다")
    @Test
    void unlinkUser_success() {
        // given
        FcmToken fcmToken = createFcmToken();
        fcmToken.linkUser(1L);

        // when
        fcmToken.unlinkUser();

        // then
        assertThat(fcmToken.getUserId()).isNull();
        assertThat(fcmToken.isLoggedIn()).isFalse();
    }

    @DisplayName("FCM 토큰을 갱신한다")
    @Test
    void updateToken_success() {
        // given
        FcmToken fcmToken = createFcmToken();
        String newToken = "new-fcm-token";
        LocalDateTime beforeUpdate = fcmToken.getLastUsedAt();

        // when
        fcmToken.updateToken(newToken);

        // then
        assertThat(fcmToken.getToken()).isEqualTo(newToken);
        assertThat(fcmToken.getLastUsedAt()).isAfter(beforeUpdate);
    }

    @DisplayName("null 토큰으로 갱신하면 예외가 발생한다")
    @Test
    void updateToken_withNull_throwsException() {
        // given
        FcmToken fcmToken = createFcmToken();

        // when & then
        assertThatThrownBy(() -> fcmToken.updateToken(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("FCM 토큰은 필수입니다.");
    }

    @DisplayName("디바이스 정보를 업데이트한다")
    @Test
    void updateDeviceInfo_success() {
        // given
        FcmToken fcmToken = createFcmToken();

        // when
        fcmToken.updateDeviceInfo("iPhone 15 Pro", "iOS 17.2", "1.1.0");

        // then
        assertThat(fcmToken.getDeviceModel()).isEqualTo("iPhone 15 Pro");
        assertThat(fcmToken.getOsVersion()).isEqualTo("iOS 17.2");
        assertThat(fcmToken.getAppVersion()).isEqualTo("1.1.0");
    }

    @DisplayName("토큰을 비활성화한다")
    @Test
    void deactivate_success() {
        // given
        FcmToken fcmToken = createFcmToken();

        // when
        fcmToken.deactivate();

        // then
        assertThat(fcmToken.isActive()).isFalse();
    }

    @DisplayName("토큰을 활성화한다")
    @Test
    void activate_success() {
        // given
        FcmToken fcmToken = createFcmToken();
        fcmToken.deactivate();

        // when
        fcmToken.activate();

        // then
        assertThat(fcmToken.isActive()).isTrue();
    }

    @DisplayName("비로그인 상태를 확인한다")
    @Test
    void isLoggedIn_whenNotLoggedIn_returnsFalse() {
        // given
        FcmToken fcmToken = createFcmToken();

        // when & then
        assertThat(fcmToken.isLoggedIn()).isFalse();
    }

    @DisplayName("로그인 상태를 확인한다")
    @Test
    void isLoggedIn_whenLoggedIn_returnsTrue() {
        // given
        FcmToken fcmToken = createFcmToken();
        fcmToken.linkUser(1L);

        // when & then
        assertThat(fcmToken.isLoggedIn()).isTrue();
    }

    private FcmToken createFcmToken() {
        return FcmToken.register(
                FcmTokenRegisterRequest.of(
                        "fcm-token-123",
                        DeviceType.ANDROID,
                        "device-id-123",
                        "Galaxy S23",
                        "Android 14",
                        "1.0.0"
                )
        );
    }
}
