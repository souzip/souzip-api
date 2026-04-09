package com.souzip.domain.user.repository;

import com.souzip.domain.user.entity.Provider;
import com.souzip.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    Optional<User> findByUserId(String userId);

    boolean existsByNickname(String nickname);

    Optional<User> findByTransferIdentifier(String transferIdentifier);

    List<User> findByProvider(Provider provider);

    @Query("""
            SELECT DISTINCT u.email FROM User u
            WHERE u.deleted = false AND u.email IS NOT NULL
            """)
    List<String> findDistinctEmailsByActiveUsers();

    long deleteByDeletedTrue();
}
