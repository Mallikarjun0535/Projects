package com.dizzion.portal.domain.maintenance.dto;

import com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent.ProgressStatus;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Optional;

@Value
public class MaintenanceEventProgressUpdateRequest {
    @NotNull
    LocalDate instanceStartDate;
    @NotNull
    ProgressStatus status;
    Optional<String> failureReason;

    public MaintenanceEventProgressUpdateRequest(LocalDate instanceStartDate, ProgressStatus status, String failureReason) {
        this.instanceStartDate = instanceStartDate;
        this.status = status;
        this.failureReason = Optional.ofNullable(failureReason);
    }
}
