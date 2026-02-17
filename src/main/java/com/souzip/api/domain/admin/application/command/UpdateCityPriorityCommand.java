package com.souzip.api.domain.admin.application.command;

public record UpdateCityPriorityCommand(
    Long cityId,
    Integer newPriority
) {}
