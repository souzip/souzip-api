package com.souzip.api.domain.search.repository;

import com.souzip.api.domain.search.document.LocationDocument;
import org.springframework.data.elasticsearch.core.SearchHit;
import java.util.List;

public interface LocationRepositoryCustom {
    List<SearchHit<LocationDocument>> searchByKeywordWithFuzzy(String keyword);
    List<SearchHit<LocationDocument>> searchWithMultipleKeywords(String[] keywords);
}
