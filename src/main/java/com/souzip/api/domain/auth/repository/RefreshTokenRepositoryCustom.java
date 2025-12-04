package com.souzip.api.domain.auth.repository;

import java.time.LocalDateTime;

public interface RefreshTokenRepositoryCustom {

    int deleteAllByExpiresAtBefore(LocalDateTime now);
}
