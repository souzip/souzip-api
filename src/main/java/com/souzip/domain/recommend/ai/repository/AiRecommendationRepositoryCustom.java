package com.souzip.domain.recommend.ai.repository;

import com.souzip.domain.souvenir.entity.Souvenir;
import com.souzip.domain.category.entity.Category;
import java.util.List;
import java.util.Optional;

public interface AiRecommendationRepositoryCustom {
    List<Souvenir> findAllByCategory(Category category);
    List<Souvenir> findAllByCountryCode(String countryCode);
    Optional<Souvenir> findLatestByUserId(Long userId);
    Optional<Souvenir> findByName(String name);
}
