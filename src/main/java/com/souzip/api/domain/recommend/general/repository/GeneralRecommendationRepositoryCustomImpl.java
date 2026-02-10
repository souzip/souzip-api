package com.souzip.api.domain.recommend.general.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.souzip.api.domain.category.entity.Category;
import com.souzip.api.domain.country.entity.QCountry;
import com.souzip.api.domain.recommend.general.dto.GeneralRecommendationStatsDto;
import com.souzip.api.domain.souvenir.entity.QSouvenir;
import com.souzip.api.domain.souvenir.entity.Souvenir;
import com.souzip.api.domain.user.entity.QUser;
import com.souzip.api.global.exception.BusinessException;
import com.souzip.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class GeneralRecommendationRepositoryCustomImpl implements GeneralRecommendationRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Souvenir> findTop10ByCategoryRecent(String categoryName) {
        QSouvenir s = QSouvenir.souvenir;
        QUser u = QUser.user;

        Category category = Category.from(categoryName)
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CATEGORY));

        return queryFactory.selectFrom(s)
            .leftJoin(s.user, u).fetchJoin()
            .where(s.category.eq(category)
                .and(s.deleted.eq(false)))
            .orderBy(s.createdAt.desc())
            .limit(10)
            .fetch();
    }

    @Override
    public List<Souvenir> findTop10ByCountry(String countryCode) {
        QSouvenir s = QSouvenir.souvenir;
        QUser u = QUser.user;

        return queryFactory.selectFrom(s)
            .leftJoin(s.user, u).fetchJoin()
            .where(s.countryCode.eq(countryCode)
                .and(s.deleted.eq(false)))
            .orderBy(s.createdAt.desc())
            .limit(10)
            .fetch();
    }

    @Override
    public List<GeneralRecommendationStatsDto> findTop3CountriesBySouvenirCount() {
        QSouvenir s = QSouvenir.souvenir;
        QCountry c = QCountry.country;

        return queryFactory
                .select(c.code, c.nameKr, s.id.count())
                .from(s)
                .join(c).on(s.countryCode.eq(c.code))
                .where(s.deleted.eq(false))
                .groupBy(c.code, c.nameKr)
                .orderBy(s.id.count().desc())
                .limit(3)
                .fetch()
                .stream()
                .map(tuple -> new GeneralRecommendationStatsDto(
                        tuple.get(c.code),
                        tuple.get(c.nameKr),
                        tuple.get(s.id.count())
                ))
                .toList();
    }
}
