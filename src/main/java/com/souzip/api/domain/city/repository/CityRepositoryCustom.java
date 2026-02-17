package com.souzip.api.domain.city.repository;

public interface CityRepositoryCustom {

    void shiftPriorityFrom(Integer priority, Long countryId);

    void pullPriorityFrom(Integer priority, Long countryId);
}
