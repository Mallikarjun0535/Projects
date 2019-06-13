package com.dizzion.portal.domain.announcement.dto;

import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;

@Builder
@Value
public class AnnouncementUpdateRequest {
    @NotBlank
    String title;
    @NotEmpty
    String[] pages;
    @NotNull
    LocalDate startDate;
    @NotNull
    LocalDate endDate;
    Set<Long> organizationIds;

    public AnnouncementUpdateRequest(String title,
                                     String[] pages,
                                     LocalDate startDate,
                                     LocalDate endDate,
                                     Set<Long> organizationIds) {
        this.title = title;
        this.pages = pages;
        this.startDate = startDate;
        this.endDate = endDate;
        this.organizationIds = Optional.ofNullable(organizationIds).orElse(emptySet());
    }
}
