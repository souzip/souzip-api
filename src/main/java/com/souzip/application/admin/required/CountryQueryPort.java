package com.souzip.application.admin.required;

import com.souzip.domain.country.entity.Country;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface CountryQueryPort {
    List<Country> getCountries(String keyword);
}