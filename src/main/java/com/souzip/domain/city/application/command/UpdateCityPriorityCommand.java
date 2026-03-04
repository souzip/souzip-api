package com.souzip.domain.city.application.command;

public record UpdateCityPriorityCommand(
    Long cityId,
    Integer newPriority
) {}
