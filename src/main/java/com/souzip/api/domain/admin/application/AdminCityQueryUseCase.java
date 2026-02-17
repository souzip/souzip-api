package com.souzip.api.domain.admin.application;

import com.souzip.api.domain.admin.application.port.CityQueryPort;
import com.souzip.api.domain.admin.application.port.CityQueryPort.CityQueryResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AdminCityQueryUseCase {

    private final CityQueryPort cityQueryPort;

    public List<CityQueryResult> getCities(Long countryId) {
        return cityQueryPort.getCities(countryId);
    }
}
