package com.souzip.api.application.search.required;

import com.souzip.api.application.search.dto.Place;
import java.util.List;

public interface PlaceSearchProvider {

    List<Place> searchByKeyword(String keyword);
}
