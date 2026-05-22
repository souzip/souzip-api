package com.souzip.domain.auth.repository;

import java.time.LocalDateTime;

public interface RefreshTokenRepositoryCustom {

    int deleteAllByExpiresAtBefore(LocalDateTime now);
}
