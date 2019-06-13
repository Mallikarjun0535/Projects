package com.dizzion.portal.domain.maintenance.dto;

import lombok.Value;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Value
public class MaintenanceEventUserAssignmentRequest {
    @NotNull
    LocalDate instanceStartDate;
    @NotNull
    Long userId;
}
