package com.souzip.domain.admin.infrastructure.persistence;

import com.souzip.domain.admin.application.port.CityQueryPort;
import com.souzip.domain.city.application.port.CityAdminPort;
import com.souzip.shared.common.dto.pagination.PaginationResponse;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CityQueryAdapter implements CityQueryPort {

    private final CityAdminPort cityAdminPort;

    @Override
    public PaginationResponse<CityQueryResult> getCities(
            Long countryId,
            String keyword,
            int pageNo,
            int pageSize
    ) {
        Page<CityAdminPort.CityAdminResult> page = cityAdminPort.getCities(
                countryId,
                keyword,
                PageRequest.of(pageNo - 1, pageSize)
        );

        List<CityQueryResult> content = page.getContent().stream()
                .map(c -> new CityQueryResult(
                        c.id(),
                        c.nameKr(),
                        c.nameEn(),
                        c.priority(),
                        c.updatedAt()
                ))
                .toList();

        return PaginationResponse.of(page, content);
    }
}
