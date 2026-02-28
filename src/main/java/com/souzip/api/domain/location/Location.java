package com.souzip.api.domain.location;

import com.souzip.api.domain.shared.BaseEntity;
import com.souzip.api.domain.shared.Coordinate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.util.Objects.requireNonNull;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location extends BaseEntity {

    private String name;

    private String address;

    private Coordinate coordinate;

    public static Location create(LocationCreateRequest createRequest) {
        Location location = new Location();

        location.name = requireNonNull(createRequest.name(), "이름은 필수입니다.");
        location.address = requireNonNull(createRequest.address(), "주소는 필수입니다.");
        location.coordinate = Coordinate.of(createRequest.latitude(), createRequest.longitude());

        return location;
    }
}
