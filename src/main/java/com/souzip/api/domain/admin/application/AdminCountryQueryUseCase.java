package com.souzip.api.domain.admin.application;

import com.souzip.api.domain.admin.application.port.CountryQueryPort.CountryQueryResult;
import java.util.List;

public interface AdminCountryQueryUseCase {

    List<CountryQueryResult> getCountries();
}
