package com.dizzion.portal.domain.application.dto;

import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Value
@Builder
public class ApplicationCreateUpdateRequest {
    @NotBlank
    String name;
    String description;
    @NotBlank
    String url;
    @NotNull
    Boolean horizon;
    @NotEmpty
    Set<Long> applicationGroupIds;
}
