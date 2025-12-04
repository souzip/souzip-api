package com.souzip.api.domain.auth.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.souzip.api.domain.auth.entity.QRefreshToken.refreshToken;

@RequiredArgsConstructor
@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public int deleteAllByExpiresAtBefore(LocalDateTime now) {
        long deletedCount = queryFactory
            .delete(refreshToken)
            .where(refreshToken.expiresAt.lt(now))
            .execute();

        return (int) deletedCount;
    }
}
