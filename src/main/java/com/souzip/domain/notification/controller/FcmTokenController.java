package com.souzip.domain.notification.controller;

import com.souzip.application.notification.FcmTokenCommandService;
import com.souzip.domain.notification.FcmToken;
import com.souzip.domain.notification.FcmTokenRegisterRequest;
import com.souzip.domain.notification.dto.FcmTokenResponse;
import com.souzip.global.common.dto.SuccessResponse;
import com.souzip.global.security.annotation.CurrentUserId;
import com.souzip.global.security.annotation.RequireAuth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/users/me/fcm-tokens")
@RestController
public class FcmTokenController {

    private final FcmTokenCommandService fcmTokenCommandService;

    // 로그인 사용자의 FCM 디바이스 토큰을 등록하거나 갱신합니다.
    @PostMapping
    @RequireAuth
    public SuccessResponse<FcmTokenResponse> register(
            @CurrentUserId Long userId,
            @Valid @RequestBody FcmTokenRegisterRequest request
    ) {
        FcmToken saved = fcmTokenCommandService.registerOrUpdate(userId, request);
        return SuccessResponse.of(FcmTokenResponse.from(saved), "FCM 토큰이 등록되었습니다.");
    }

    // 로그아웃 등으로 해당 디바이스 토큰을 비활성화합니다.
    @DeleteMapping
    @RequireAuth
    public SuccessResponse<Void> deactivate(
            @CurrentUserId Long userId,
            @RequestParam String deviceId
    ) {
        fcmTokenCommandService.deactivateByDevice(userId, deviceId);
        return SuccessResponse.of("FCM 토큰이 비활성화되었습니다.");
    }
}
