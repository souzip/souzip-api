package com.souzip.api.domain.city.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.souzip.api.domain.city.entity.City;
import com.souzip.api.domain.city.entity.QCity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
    public void shiftPriorityFrom(Integer priority, Long countryId) {
        queryFactory
            .update(city)
            .set(city.priority, city.priority.add(1))
            .where(
                city.priority.goe(priority),
                city.country.id.eq(countryId)
            )
            .execute();
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
}
