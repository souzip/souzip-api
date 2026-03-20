package com.souzip.application.notification.required;

import com.souzip.domain.notification.FcmToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface FcmTokenRepository extends Repository<FcmToken, Long> {

    FcmToken save(FcmToken fcmToken);

    Optional<FcmToken> findByToken(String token);

    Optional<FcmToken> findByDeviceId(String deviceId);

    Optional<FcmToken> findByUserIdAndDeviceId(Long userId, String deviceId);

    List<FcmToken> findByUserIdAndIsActiveTrue(Long userId);

    List<FcmToken> findAllByIsActiveTrue();

    void delete(FcmToken fcmToken);

    void deleteByDeviceId(String deviceId);

    boolean existsByToken(String token);

    boolean existsByDeviceId(String deviceId);
}
