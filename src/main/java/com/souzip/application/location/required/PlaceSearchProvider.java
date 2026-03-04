package com.souzip.application.location.required;

import com.souzip.application.location.dto.SearchPlace;
import java.util.List;

public interface PlaceSearchProvider {

    List<SearchPlace> searchByKeyword(String keyword);
}
