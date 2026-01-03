package com.souzip.api.domain.recommend.ai.repository;

import com.souzip.api.domain.souvenir.entity.Souvenir;
import com.souzip.api.domain.category.entity.Category;
import java.util.List;
import java.util.Optional;

public interface AiRecommendationRepositoryCustom {
    List<Souvenir> findAllByCategory(Category category);
    List<Souvenir> findAllByCountryCode(String countryCode);
    Optional<Souvenir> findLatestByUserId(Long userId);
    Optional<Souvenir> findByName(String name);
}
