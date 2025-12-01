package com.souzip.api.domain.country.repository;

import com.souzip.api.domain.country.entity.Country;
import java.util.List;

public interface CountryRepositoryCustom {

    List<Country> findByNameContaining(String name);
}
