package com.souzip.application.admin.required;

import com.souzip.domain.country.entity.Country;
import java.util.List;

public interface CountryQueryPort {
    List<Country> getCountries(String keyword);
}
