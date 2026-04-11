package com.souzip.application.notification.required;

import com.souzip.domain.notification.FcmToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface FcmTokenRepository extends Repository<FcmToken, Long> {

    FcmToken save(FcmToken fcmToken);

    Optional<FcmToken> findByToken(String token);

    Optional<FcmToken> findByDeviceId(String deviceId);

    Optional<FcmToken> findByUserIdAndDeviceId(Long userId, String deviceId);

    List<FcmToken> findByUserIdAndActiveTrue(Long userId);

    List<FcmToken> findAllByActiveTrue();

    @Query("SELECT ft FROM FcmToken ft WHERE ft.active = true AND ft.userId IN " +
           "(SELECT ua.user.id FROM UserAgreement ua WHERE ua.marketingConsent = true)")
    List<FcmToken> findAllActiveWithMarketingConsent();

    void delete(FcmToken fcmToken);

    void deleteByDeviceId(String deviceId);

    boolean existsByToken(String token);

    boolean existsByDeviceId(String deviceId);
}
