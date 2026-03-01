package com.souzip.api.application.location.provided;

import com.souzip.api.application.location.dto.SearchResult;

public interface LocationSearch {

    SearchResult search(String keyword);
}
