package com.souzip.api.domain.search.repository;

import com.souzip.api.domain.search.document.LocationDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface LocationRepository extends ElasticsearchRepository<LocationDocument, String>, LocationRepositoryCustom {

    @Query("""
        {
          "bool": {
            "must": [
              { "term": { "type": "city" } }
            ],
            "should": [
              { "term": { "countryNameKr.keyword": "?0" } },
              { "term": { "countryNameEn.keyword": "?1" } }
            ],
            "minimum_should_match": 1
          }
        }
        """)
    List<LocationDocument> findCitiesByCountryNameOrderByPriority(
        String countryNameKr,
        String countryNameEn,
        Pageable pageable
    );
}
