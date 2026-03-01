package com.souzip.domain.search.repository;

import com.souzip.domain.city.entity.City;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface LocationSearchRepository extends Repository<City, Long> {

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
    List<City> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT COUNT(c) FROM City c
            WHERE c.nameKr = :keyword
               OR c.country.id IN (
                   SELECT co.id FROM Country co 
                   WHERE co.nameKr = :keyword
               )
            """)
    long countByKeyword(@Param("keyword") String keyword);
}
