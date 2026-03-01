package com.souzip.application.location.provided;

import com.souzip.application.location.dto.SearchResult;

public interface LocationSearch {

    SearchResult search(String keyword);
}
