package com.souzip.domain.country.application.port;

import com.souzip.domain.country.entity.Country;
import java.util.List;

public interface CountryAdminPort {

    List<CountryAdminResult> getCountries(String keyword);

    record CountryAdminResult(Long id, String nameKr) {
        public static CountryAdminResult from(Country country) {
            return new CountryAdminResult(
                country.getId(),
                country.getNameKr()
            );
        }
    }
}
