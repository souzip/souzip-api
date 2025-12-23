package com.souzip.api.domain.user.repository;

import com.souzip.api.domain.user.entity.Provider;
import com.souzip.api.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    Optional<User> findByUserId(String userId);

    boolean existsByNickname(String nickname);
}
