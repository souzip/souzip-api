package com.souzip.api.domain.user.repository;

import com.souzip.api.domain.user.entity.Provider;
import com.souzip.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    Optional<User> findByUserId(String userId);
}
