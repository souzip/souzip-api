package com.souzip.api.domain.souvenir.repository;

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

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Souvenir> findByIdWithUser(Long id) {
        QSouvenir s = QSouvenir.souvenir;
        QUser u = QUser.user;

        Souvenir souvenir = queryFactory.selectFrom(s)
            .leftJoin(s.user, u).fetchJoin()
            .where(s.id.eq(id).and(s.deleted.eq(false)))
            .fetchOne();

        return Optional.ofNullable(souvenir);
    }

    @Override
    public Page<Souvenir> findByUserWithUser(User user, Pageable pageable) {
        QSouvenir s = QSouvenir.souvenir;
        QUser u = QUser.user;

        // 데이터 조회
        List<Souvenir> content = queryFactory.selectFrom(s)
            .leftJoin(s.user, u).fetchJoin()
            .where(s.user.eq(user).and(s.deleted.eq(false)))
            .orderBy(s.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 카운트 쿼리
        Long total = queryFactory.select(s.count())
            .from(s)
            .where(s.user.eq(user).and(s.deleted.eq(false)))
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
