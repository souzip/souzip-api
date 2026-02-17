package com.souzip.api.domain.city.application.port;

import com.souzip.api.domain.city.entity.City;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CityAdminPort {

    Page<CityAdminResult> getCities(Long countryId, String keyword, Pageable pageable);

    record CityAdminResult(
        Long id,
        String nameKr,
        Integer priority,
        LocalDateTime updatedAt
    ) {
        public static CityAdminResult from(City city) {
            return new CityAdminResult(
                city.getId(),
                city.getNameKr(),
                city.getPriority(),
                city.getUpdatedAt()
            );
        }
    }
}
