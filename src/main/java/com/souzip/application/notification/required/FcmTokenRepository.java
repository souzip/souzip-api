package com.souzip.application.notification.required;

import com.souzip.domain.notification.FcmToken;
import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository {

    FcmToken save(FcmToken fcmToken);

    Optional<FcmToken> findByToken(String token);

    Optional<FcmToken> findByDeviceId(String deviceId);

    Optional<FcmToken> findByUserIdAndDeviceId(Long userId, String deviceId);

    List<FcmToken> findActiveTokensByUserId(Long userId);

    List<FcmToken> findAllActiveTokens();

    void delete(FcmToken fcmToken);

    void deleteByDeviceId(String deviceId);

    boolean existsByToken(String token);

    boolean existsByDeviceId(String deviceId);
}
