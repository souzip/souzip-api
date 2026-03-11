package com.souzip.domain.admin.application.command;

public record AdminUpdateCityPriorityCommand(
    Long cityId,
    Integer newPriority
) {}
