package com.souzip.api.domain.city.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.city.entity.QCity;
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
    public List<City> findByCountryIdAndPriorityGoeOrderByPriorityAsc(Long countryId, Integer priority) {
        return queryFactory
            .selectFrom(city)
            .where(
                city.country.id.eq(countryId),
                city.priority.goe(priority)
            )
            .orderBy(city.priority.asc())
            .fetch();
    }

    @Override
    public void pullPriorityFrom(Integer priority, Long countryId) {
        queryFactory
            .update(city)
            .set(city.priority, city.priority.subtract(1))
            .where(
                city.priority.gt(priority),
                city.country.id.eq(countryId)
            )
            .execute();
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
