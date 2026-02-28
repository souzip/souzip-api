package com.souzip.api.application.search.required;

import com.souzip.api.domain.location.Location;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface LocationRepository extends Repository<Location, Long> {

    List<Location> findByNameContaining(String keyword);
}
