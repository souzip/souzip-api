package com.souzip.api.application.search.required;

import com.souzip.api.domain.city.entity.City;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CitySearchRepository extends Repository<City, Long> {

    @Query("""
            SELECT c FROM City c
            LEFT JOIN FETCH c.country
            WHERE c.nameKr = :keyword
               OR c.country.nameKr = :keyword
            ORDER BY
                CASE
                    WHEN c.nameKr = :keyword THEN 1
                    WHEN c.country.nameKr = :keyword THEN 2
                    ELSE 3
                END,
                c.priority ASC NULLS LAST,
                c.nameKr ASC
            """)
    List<City> searchByKeyword(@Param("keyword") String keyword);
}
