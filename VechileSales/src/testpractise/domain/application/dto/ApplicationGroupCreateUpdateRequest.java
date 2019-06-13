package com.dizzion.portal.domain.application.dto;

import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Value
@Builder
public class ApplicationGroupCreateUpdateRequest {
    @NotBlank
    String name;
    @NotNull
    Boolean enabled;
    @NotEmpty
    Set<Long> organizationIds;
}
