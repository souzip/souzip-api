package com.souzip.application.notification;

import com.souzip.application.notification.required.FcmTokenRepository;
import com.souzip.domain.notification.FcmToken;
import com.souzip.domain.notification.FcmTokenRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FcmTokenCommandService {

    private final FcmTokenRepository fcmTokenRepository;

    // 로그인 사용자 기준으로 FCM 토큰을 등록하거나 동일 디바이스·토큰에 맞춰 갱신합니다.
    public FcmToken registerOrUpdate(Long userId, FcmTokenRegisterRequest request) {
        return fcmTokenRepository.findByToken(request.fcmToken())
                .map(existing -> syncExistingToken(existing, userId, request))
                .orElseGet(() -> fcmTokenRepository.findByUserIdAndDeviceId(userId, request.deviceId())
                        .map(existing -> updateExistingDeviceRow(existing, request, userId))
                        .orElseGet(() -> createNew(userId, request)));
    }

    private FcmToken syncExistingToken(FcmToken existing, Long userId, FcmTokenRegisterRequest request) {
        existing.linkUser(userId);
        existing.syncDeviceIdentity(request.deviceType(), request.deviceId());
        existing.updateDeviceInfo(request.deviceModel(), request.osVersion(), request.appVersion());
        existing.activate();
        return fcmTokenRepository.save(existing);
    }

    private FcmToken updateExistingDeviceRow(FcmToken existing, FcmTokenRegisterRequest request, Long userId) {
        existing.updateToken(request.fcmToken());
        existing.syncDeviceIdentity(request.deviceType(), request.deviceId());
        existing.updateDeviceInfo(request.deviceModel(), request.osVersion(), request.appVersion());
        existing.linkUser(userId);
        existing.activate();
        return fcmTokenRepository.save(existing);
    }

    private FcmToken createNew(Long userId, FcmTokenRegisterRequest request) {
        FcmToken created = FcmToken.register(request);
        created.linkUser(userId);
        return fcmTokenRepository.save(created);
    }

    // 해당 디바이스의 푸시 토큰을 비활성화합니다.
    public void deactivateByDevice(Long userId, String deviceId) {
        fcmTokenRepository.findByUserIdAndDeviceId(userId, deviceId)
                .ifPresent(token -> {
                    token.deactivate();
                    fcmTokenRepository.save(token);
                });
    }
}
