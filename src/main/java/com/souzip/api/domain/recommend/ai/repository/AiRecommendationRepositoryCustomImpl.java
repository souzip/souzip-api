package com.souzip.api.domain.recommend.ai.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.file.entity.QFile;
import com.souzip.api.domain.souvenir.entity.QSouvenir;
import com.souzip.api.domain.souvenir.entity.Souvenir;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AiRecommendationRepositoryCustomImpl implements AiRecommendationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Souvenir> findAllByCategory(Category category) {
        QSouvenir s = QSouvenir.souvenir;
        QFile f = QFile.file;

        return queryFactory.selectFrom(s)
                .leftJoin(s.files, f).fetchJoin()
                .where(s.category.eq(category).and(s.deleted.eq(false)))
                .fetch();
    }

    @Override
    public Optional<Souvenir> findLatestByUserId(Long userId) {
        QSouvenir s = QSouvenir.souvenir;
        QFile f = QFile.file;

        Souvenir latest = queryFactory.selectFrom(s)
                .leftJoin(s.files, f).fetchJoin()
                .where(s.user.id.eq(userId).and(s.deleted.eq(false)))
                .orderBy(s.createdAt.desc())
                .fetchFirst();

        return Optional.ofNullable(latest);
    }

    @Override
    public List<Souvenir> findAllByCountryCode(String countryCode) {
        QSouvenir s = QSouvenir.souvenir;
        QFile f = QFile.file;

        return queryFactory.selectFrom(s)
                .leftJoin(s.files, f).fetchJoin()
                .where(s.countryCode.eq(countryCode).and(s.deleted.eq(false)))
                .limit(10)
                .fetch();
    }

    @Override
    public Optional<Souvenir> findByName(String name) {
        QSouvenir s = QSouvenir.souvenir;
        QFile f = QFile.file;

        Souvenir souvenir = queryFactory.selectFrom(s)
                .leftJoin(s.files, f).fetchJoin()
                .where(s.name.eq(name).and(s.deleted.eq(false)))
                .fetchFirst();

        return Optional.ofNullable(souvenir);
    }
}
