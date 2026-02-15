package com.souzip.api.domain.souvenir.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.souzip.api.domain.souvenir.entity.QSouvenir;
import com.souzip.api.domain.souvenir.entity.Souvenir;
import com.souzip.api.domain.user.entity.QUser;
import com.souzip.api.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class SouvenirRepositoryCustomImpl implements SouvenirRepositoryCustom {

    private static final QSouvenir SOUVENIR = QSouvenir.souvenir;
    private static final QUser USER = QUser.user;

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Souvenir> findByIdWithUser(Long id) {
        Souvenir souvenir = queryFactory
            .selectFrom(SOUVENIR)
            .leftJoin(SOUVENIR.user, USER).fetchJoin()
            .where(
                idEquals(id),
                isNotDeleted()
            )
            .fetchOne();

        return Optional.ofNullable(souvenir);
    }

    @Override
    public Page<Souvenir> findByUserWithUser(User user, Pageable pageable) {
        List<Souvenir> content = fetchSouvenirsByUser(user, pageable);
        Long total = countSouvenirsByUser(user);

        return new PageImpl<>(content, pageable, total);
    }

    private List<Souvenir> fetchSouvenirsByUser(User user, Pageable pageable) {
        return queryFactory
            .selectFrom(SOUVENIR)
            .leftJoin(SOUVENIR.user, USER).fetchJoin()
            .where(
                userEquals(user),
                isNotDeleted()
            )
            .orderBy(SOUVENIR.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }

    private Long countSouvenirsByUser(User user) {
        Long count = queryFactory
            .select(SOUVENIR.count())
            .from(SOUVENIR)
            .where(
                userEquals(user),
                isNotDeleted()
            )
            .fetchOne();

        return getCountOrZero(count);
    }

    private Long getCountOrZero(Long count) {
        if (count == null) {
            return 0L;
        }
        return count;
    }

    private BooleanExpression idEquals(Long id) {
        return SOUVENIR.id.eq(id);
    }

    private BooleanExpression userEquals(User user) {
        return SOUVENIR.user.eq(user);
    }

    private BooleanExpression isNotDeleted() {
        return SOUVENIR.deleted.eq(Boolean.FALSE);
    }
}
