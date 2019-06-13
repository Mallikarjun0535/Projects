package com.dizzion.portal.domain.organization.dto;

import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Set;

@Value
@Builder
public class OrganizationGroupCreateUpdateRequest {
    @NotBlank
    String name;
    @NotEmpty
    Set<Long> organizationIds;
}
