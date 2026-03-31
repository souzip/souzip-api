package com.souzip.domain.souvenir.repository;

import com.souzip.domain.souvenir.entity.Souvenir;
import com.souzip.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SouvenirRepository extends JpaRepository<Souvenir, Long>, SouvenirRepositoryCustom {

    Optional<Souvenir> findByIdAndDeletedFalse(Long id);
    Page<Souvenir> findByUserAndDeletedFalse(User user, Pageable pageable);

    @Query(value = """
    SELECT s.id,
           s.name,
           s.category,
           s.purpose,
           s.local_price,
           s.krw_price,
           s.currency_symbol,
           f.storage_key,
           s.latitude,
           s.longitude,
           s.address,
           COUNT(w.id) AS wishlist_count
    FROM souvenir s
    LEFT JOIN file f
           ON f.entity_type = 'Souvenir'
           AND f.entity_id = s.id
           AND f.display_order = 1
    LEFT JOIN wishlist w ON w.souvenir_id = s.id
    WHERE s.deleted = false
      AND ST_DWithin(
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
            ST_SetSRID(ST_MakePoint(s.longitude, s.latitude), 4326)::geography,
            :radiusMeter
      )
    GROUP BY s.id, s.name, s.category, s.purpose, s.local_price,
             s.krw_price, s.currency_symbol, f.storage_key,
             s.latitude, s.longitude, s.address
    ORDER BY ST_Distance(
            ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
            ST_SetSRID(ST_MakePoint(s.longitude, s.latitude), 4326)::geography
    )
    LIMIT 10
    """, nativeQuery = true)
    List<Object[]> findNearbySouvenirs(
        @Param("latitude") double latitude,
        @Param("longitude") double longitude,
        @Param("radiusMeter") double radiusMeter);
}
