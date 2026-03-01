package com.souzip.api.application.location.required;

import com.souzip.api.application.location.dto.SearchPlace;
import java.util.List;

public interface PlaceSearchProvider {

    List<SearchPlace> searchByKeyword(String keyword);
}
