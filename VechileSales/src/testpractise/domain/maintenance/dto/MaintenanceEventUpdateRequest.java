package com.dizzion.portal.domain.maintenance.dto;

import com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent.Severity;
import com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventEntity;
import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

@Builder
@Value
public class MaintenanceEventUpdateRequest {
    @NotNull
    MaintenanceEventEntity.Type type;
    @NotBlank
    String title;
    String message;
    @NotNull
    ZonedDateTime startDateTime;
    @NotNull
    ZonedDateTime endDateTime;
    String timezoneOffset;
    @NotEmpty
    Set<Long> organizationIds;
    @NotNull
    Boolean reminder;
    String cron;
    @NotBlank
    String recurrencePatternName;
    LocalDate repeatUntil;
    @NotNull
    Severity severity;
    Long assignedUserId;

    public Optional<Long> getAssignedUserId() {
        return Optional.ofNullable(assignedUserId);
    }

    public Optional<String> getCron() {
        return Optional.ofNullable(cron);
    }
}
