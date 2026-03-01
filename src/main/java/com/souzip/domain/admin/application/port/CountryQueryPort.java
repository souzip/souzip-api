package com.souzip.domain.admin.application.port;

import java.util.List;

public interface CountryQueryPort {
    List<CountryQueryResult> getCountries(String keyword);

    record CountryQueryResult(
        Long id,
        String nameKr
    ) {}
}
