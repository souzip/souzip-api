package com.souzip.domain.wishlist.repository;

import com.souzip.domain.wishlist.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
