package com.souzip.api.domain.admin.application.command;

public record AdminUpdateCityPriorityCommand(
    Long cityId,
    Integer newPriority
) {}
