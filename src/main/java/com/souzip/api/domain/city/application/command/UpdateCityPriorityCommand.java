package com.souzip.api.domain.city.application.command;

public record UpdateCityPriorityCommand(
    Long cityId,
    Integer newPriority
) {}
