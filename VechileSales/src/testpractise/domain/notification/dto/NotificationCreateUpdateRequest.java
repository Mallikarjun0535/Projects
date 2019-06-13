package com.dizzion.portal.domain.notification.dto;

import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

@Builder
@Value
public class NotificationCreateUpdateRequest {
    @NotBlank
    String title;
    @NotBlank
    String message;
    @NotNull
    LocalDate startDate;
    @NotNull
    LocalDate endDate;
    @NotEmpty
    Set<Long> organizationIds;
}
