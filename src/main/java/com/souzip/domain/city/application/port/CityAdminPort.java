package com.souzip.domain.city.application.port;

import com.souzip.domain.city.entity.City;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CityAdminPort {

    Page<CityAdminResult> getCities(Long countryId, String keyword, Pageable pageable);

    record CityAdminResult(
        Long id,
        String nameKr,
        String nameEn,
        Integer priority,
        LocalDateTime updatedAt
    ) {
        public static CityAdminResult from(City city) {
            return new CityAdminResult(
                city.getId(),
                city.getNameKr(),
                city.getNameEn(),
                city.getPriority(),
                city.getUpdatedAt()
            );
        }
    }
}
