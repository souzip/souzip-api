package com.souzip.domain.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class FcmTokenRegisterRequestTest {

    @DisplayName("정상적으로 요청 객체를 생성한다")
    @Test
    void create_success() {
        // when
        FcmTokenRegisterRequest request = FcmTokenRegisterRequest.of(
                "fcm-token-123",
                DeviceType.ANDROID,
                "device-id-123",
                "Galaxy S23",
                "Android 14",
                "1.0.0"
        );

        // then
        assertThat(request.fcmToken()).isEqualTo("fcm-token-123");
        assertThat(request.deviceType()).isEqualTo(DeviceType.ANDROID);
        assertThat(request.deviceId()).isEqualTo("device-id-123");
        assertThat(request.deviceModel()).isEqualTo("Galaxy S23");
        assertThat(request.osVersion()).isEqualTo("Android 14");
        assertThat(request.appVersion()).isEqualTo("1.0.0");
    }

    @DisplayName("선택 필드 없이 요청 객체를 생성한다")
    @Test
    void create_withoutOptionalFields() {
        // when
        FcmTokenRegisterRequest request = FcmTokenRegisterRequest.of(
                "fcm-token-123",
                DeviceType.IOS,
                "device-id-456",
                null,
                null,
                null
        );

        // then
        assertThat(request.fcmToken()).isEqualTo("fcm-token-123");
        assertThat(request.deviceType()).isEqualTo(DeviceType.IOS);
        assertThat(request.deviceModel()).isNull();
        assertThat(request.osVersion()).isNull();
        assertThat(request.appVersion()).isNull();
    }

    @DisplayName("FCM 토큰이 null이면 예외가 발생한다")
    @Test
    void create_withNullFcmToken_throwsException() {
        // when & then
        assertThatThrownBy(() -> FcmTokenRegisterRequest.of(
                null,
                DeviceType.ANDROID,
                "device-id-123",
                null,
                null,
                null
        )).isInstanceOf(NullPointerException.class)
                .hasMessage("FCM 토큰은 필수입니다.");
    }

    @DisplayName("FCM 토큰이 비어있으면 예외가 발생한다")
    @ValueSource(strings = {"", " ", "  "})
    @ParameterizedTest
    void create_withEmptyFcmToken_throwsException(String emptyToken) {
        // when & then
        assertThatThrownBy(() -> FcmTokenRegisterRequest.of(
                emptyToken,
                DeviceType.ANDROID,
                "device-id-123",
                null,
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("FCM 토큰은 비어있을 수 없습니다.");
    }

    @DisplayName("FCM 토큰이 최대 길이를 초과하면 예외가 발생한다")
    @Test
    void create_withTooLongFcmToken_throwsException() {
        // given
        String tooLongToken = "a".repeat(256);

        // when & then
        assertThatThrownBy(() -> FcmTokenRegisterRequest.of(
                tooLongToken,
                DeviceType.ANDROID,
                "device-id-123",
                null,
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FCM 토큰은 255자를 초과할 수 없습니다");
    }

    @DisplayName("디바이스 타입이 null이면 예외가 발생한다")
    @Test
    void create_withNullDeviceType_throwsException() {
        // when & then
        assertThatThrownBy(() -> FcmTokenRegisterRequest.of(
                "fcm-token-123",
                null,
                "device-id-123",
                null,
                null,
                null
        )).isInstanceOf(NullPointerException.class)
                .hasMessage("디바이스 타입은 필수입니다.");
    }

    @DisplayName("디바이스 ID가 null이면 예외가 발생한다")
    @Test
    void create_withNullDeviceId_throwsException() {
        // when & then
        assertThatThrownBy(() -> FcmTokenRegisterRequest.of(
                "fcm-token-123",
                DeviceType.ANDROID,
                null,
                null,
                null,
                null
        )).isInstanceOf(NullPointerException.class)
                .hasMessage("디바이스 ID는 필수입니다.");
    }

    @DisplayName("디바이스 ID가 비어있으면 예외가 발생한다")
    @ValueSource(strings = {"", " ", "  "})
    @ParameterizedTest
    void create_withEmptyDeviceId_throwsException(String emptyDeviceId) {
        // when & then
        assertThatThrownBy(() -> FcmTokenRegisterRequest.of(
                "fcm-token-123",
                DeviceType.ANDROID,
                emptyDeviceId,
                null,
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("디바이스 ID는 비어있을 수 없습니다.");
    }

    @DisplayName("디바이스 ID가 최대 길이를 초과하면 예외가 발생한다")
    @Test
    void create_withTooLongDeviceId_throwsException() {
        // given
        String tooLongDeviceId = "a".repeat(101);

        // when & then
        assertThatThrownBy(() -> FcmTokenRegisterRequest.of(
                "fcm-token-123",
                DeviceType.ANDROID,
                tooLongDeviceId,
                null,
                null,
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("디바이스 ID는 100자를 초과할 수 없습니다");
    }

    @DisplayName("FCM 토큰 앞뒤 공백을 제거한다")
    @Test
    void create_trimsFcmToken() {
        // when
        FcmTokenRegisterRequest request = FcmTokenRegisterRequest.of(
                "  fcm-token-123  ",
                DeviceType.ANDROID,
                "device-id-123",
                null,
                null,
                null
        );

        // then
        assertThat(request.fcmToken()).isEqualTo("fcm-token-123");
    }

    @DisplayName("디바이스 ID 앞뒤 공백을 제거한다")
    @Test
    void create_trimsDeviceId() {
        // when
        FcmTokenRegisterRequest request = FcmTokenRegisterRequest.of(
                "fcm-token-123",
                DeviceType.ANDROID,
                "  device-id-123  ",
                null,
                null,
                null
        );

        // then
        assertThat(request.deviceId()).isEqualTo("device-id-123");
    }
}
