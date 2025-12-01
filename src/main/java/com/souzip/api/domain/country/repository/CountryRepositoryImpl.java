package com.souzip.api.domain.country.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.souzip.api.domain.country.entity.Country;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.souzip.api.domain.country.entity.QCountry.country;

@RequiredArgsConstructor
@Repository
public class CountryRepositoryImpl implements CountryRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Country> findByNameContaining(String name) {
        return queryFactory
            .selectFrom(country)
            .where(
                country.nameEn.containsIgnoreCase(name)
                    .or(country.nameKr.contains(name))
            )
            .fetch();
    }
}
