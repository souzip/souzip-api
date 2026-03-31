package com.souzip.domain.wishlist.repository;

import com.souzip.domain.wishlist.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    boolean existsByUserIdAndSouvenirId(Long userId, Long souvenirId);

    @Modifying
    @Query("DELETE FROM Wishlist w WHERE w.user.id = :userId AND w.souvenir.id = :souvenirId")
    void deleteByUserIdAndSouvenirId(@Param("userId") Long userId, @Param("souvenirId") Long souvenirId);

    @Query(
            value = "SELECT w FROM Wishlist w JOIN FETCH w.souvenir WHERE w.user.id = :userId ORDER BY w.createdAt DESC",
            countQuery = "SELECT COUNT(w) FROM Wishlist w WHERE w.user.id = :userId"
    )
    Page<Wishlist> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT w.souvenir.id FROM Wishlist w WHERE w.user.id = :userId")
    Set<Long> findSouvenirIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.souvenir.id = :souvenirId")
    long countBySouvenirId(@Param("souvenirId") Long souvenirId);

    @Query("SELECT w.souvenir.id, COUNT(w) FROM Wishlist w WHERE w.souvenir.id IN :souvenirIds GROUP BY w.souvenir.id")
    List<Object[]> countBySouvenirIdsRaw(@Param("souvenirIds") List<Long> souvenirIds);

    default Map<Long, Long> countBySouvenirIds(List<Long> souvenirIds) {
        return countBySouvenirIdsRaw(souvenirIds).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }
}
