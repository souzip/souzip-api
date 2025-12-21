package com.souzip.api.domain.search.repository;

import com.souzip.api.domain.search.document.LocationDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface LocationRepository extends ElasticsearchRepository<LocationDocument, String>, LocationRepositoryCustom {

    List<LocationDocument> findByCountryNameKrOrCountryNameEn(
        String countryNameKr,
        String countryNameEn,
        Pageable pageable
    );
}
