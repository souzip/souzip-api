package com.souzip.api.domain.city.application.query;

import com.souzip.api.domain.admin.application.port.CityQueryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CityQueryAdapter implements CityQueryPort {

    private final CityQueryService cityQueryService;

    @Override
    public List<CityQueryResult> getCities(Long countryId) {
        return cityQueryService.getCities(countryId).stream()
            .map(result -> new CityQueryResult(
                result.id(),
                result.nameKr(),
                result.priority(),
                result.updatedAt()
            ))
            .toList();
    }
}
