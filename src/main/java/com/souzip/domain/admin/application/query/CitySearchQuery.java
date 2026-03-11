package com.souzip.domain.admin.application.query;

public record CitySearchQuery(
    Long countryId,
    String keyword,
    int pageNo,
    int pageSize
) {
    public static CitySearchQuery of(Long countryId, String keyword, int pageNo, int pageSize) {
        return new CitySearchQuery(countryId, keyword, pageNo, pageSize);
    }
}
