package com.souzip.api.application.search.provided;

import com.souzip.api.application.search.dto.SearchResult;

public interface LocationSearch {

    SearchResult search(String keyword);
}
