package com.souzip.domain.city.event;

public record CityPriorityUpdatedEvent(
    Long cityId,
    Integer oldPriority,
    Integer newPriority
) {
    public static CityPriorityUpdatedEvent of(Long cityId, Integer oldPriority, Integer newPriority) {
        return new CityPriorityUpdatedEvent(cityId, oldPriority, newPriority);
    }
}
