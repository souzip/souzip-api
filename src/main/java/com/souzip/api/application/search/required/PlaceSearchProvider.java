package com.souzip.api.application.search.required;

import com.souzip.api.application.search.dto.SearchPlace;
import java.util.List;

public interface PlaceSearchProvider {

    List<SearchPlace> searchByKeyword(String keyword);
}
