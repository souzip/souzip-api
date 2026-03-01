package com.souzip.domain.souvenir.repository;

import com.souzip.domain.souvenir.entity.Souvenir;
import com.souzip.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SouvenirRepositoryCustom {
    Optional<Souvenir> findByIdWithUser(Long id);
    Page<Souvenir> findByUserWithUser(User user, Pageable pageable);
}
