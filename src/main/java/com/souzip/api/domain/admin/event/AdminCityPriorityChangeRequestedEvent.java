package com.souzip.api.domain.admin.event;

public record AdminCityPriorityChangeRequestedEvent(
    Long cityId,
    Integer newPriority
) {
    public static AdminCityPriorityChangeRequestedEvent of(Long cityId, Integer newPriority) {
        return new AdminCityPriorityChangeRequestedEvent(cityId, newPriority);
    }
}
