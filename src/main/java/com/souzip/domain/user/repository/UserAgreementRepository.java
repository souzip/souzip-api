package com.souzip.domain.user.repository;

import com.souzip.domain.user.entity.User;
import com.souzip.domain.user.entity.UserAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAgreementRepository extends JpaRepository<UserAgreement, Long> {

    Optional<UserAgreement> findByUser(User user);

    boolean existsByUser(User user);
}
