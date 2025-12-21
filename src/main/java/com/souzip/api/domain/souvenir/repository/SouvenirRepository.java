package com.souzip.api.domain.souvenir.repository;

import com.souzip.api.domain.souvenir.entity.Souvenir;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SouvenirRepository extends JpaRepository<Souvenir, Long> {
    Optional<Souvenir> findByIdAndDeletedFalse(Long id);

    @Query(value = """
        SELECT s.id,
               s.name,
               s.category,
               f.storage_key,
               ST_Distance(
                   ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
                   ST_SetSRID(ST_MakePoint(s.longitude, s.latitude), 4326)::geography
               ) AS distance
        FROM souvenir s
        LEFT JOIN file f
               ON f.entity_type = 'Souvenir'
               AND f.entity_id = s.id
               AND f.display_order = 1
        WHERE s.deleted = false
          AND ST_DWithin(
                ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
                ST_SetSRID(ST_MakePoint(s.longitude, s.latitude), 4326)::geography,
                :radiusMeter
          )
        ORDER BY distance
        LIMIT 10
        """, nativeQuery = true)
    List<Object[]> findNearbySouvenirs(@Param("latitude") double latitude,
                                       @Param("longitude") double longitude, double radiusMeter);
}
