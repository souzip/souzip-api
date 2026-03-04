package com.souzip.domain.city.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.souzip.domain.city.entity.City;
import com.souzip.domain.city.entity.QCity;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CityRepositoryImpl implements CityRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QCity city = QCity.city;

    @Override
    public Optional<City> findByIdWithLock(Long cityId) {
        City result = queryFactory
                .selectFrom(city)
                .where(city.id.eq(cityId))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public List<City> findByCountryId(Long countryId) {
        return queryFactory
                .selectFrom(city)
                .where(city.country.id.eq(countryId))
                .orderBy(city.priority.asc().nullsLast(), city.nameKr.asc())
                .fetch();
    }

    @Override
    public List<City> findByCountryIdAndPriorityGoeOrderByPriorityAscWithLock(Long countryId, Integer priority) {
        return queryFactory
                .selectFrom(city)
                .where(
                        city.country.id.eq(countryId),
                        city.priority.goe(priority)
                )
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .orderBy(city.priority.asc())
                .fetch();
    }

    @Override
    public List<City> findByCountryIdAndPriorityBetweenOrderByPriorityAscWithLock(
            Long countryId, Integer startInclusive, Integer endInclusive
    ) {
        return queryFactory
                .selectFrom(city)
                .where(
                        city.country.id.eq(countryId),
                        city.priority.between(startInclusive, endInclusive)
                )
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .orderBy(city.priority.asc())
                .fetch();
    }

    @Override
    public Page<City> findByCountryIdWithPaging(Long countryId, Pageable pageable) {
        List<City> content = queryFactory
                .selectFrom(city)
                .where(city.country.id.eq(countryId))
                .orderBy(city.priority.asc().nullsLast(), city.nameKr.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(city.count())
                .from(city)
                .where(city.country.id.eq(countryId));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<City> searchByKeyword(Long countryId, String keyword, Pageable pageable) {
        List<City> content = queryFactory
                .selectFrom(city)
                .where(
                        city.country.id.eq(countryId),
                        keywordContains(keyword)
                )
                .orderBy(city.priority.asc().nullsLast(), city.nameKr.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(city.count())
                .from(city)
                .where(
                        city.country.id.eq(countryId),
                        keywordContains(keyword)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return city.nameKr.containsIgnoreCase(keyword)
                .or(city.nameEn.containsIgnoreCase(keyword));
    }
}
