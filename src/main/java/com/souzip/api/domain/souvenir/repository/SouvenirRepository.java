package com.souzip.api.domain.souvenir.repository;

import com.souzip.api.domain.souvenir.entity.Souvenir;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SouvenirRepository extends JpaRepository<Souvenir, Long> {
    Optional<Souvenir> findByIdAndDeletedFalse(Long id);
}
