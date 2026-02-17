package com.souzip.api.domain.admin.application.port;

import java.util.List;

public interface CountryQueryPort {
    List<CountryQueryResult> getCountries();

    record CountryQueryResult(
        Long id,
        String nameKr
    ) {}
}
