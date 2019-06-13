package com.dizzion.portal.domain.dizzionteam.dto;

import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Size;
import java.util.Set;

@Value
@Builder
public class DizzionTeamCreateUpdateRequest {
    @NotBlank
    String name;
    @NotEmpty
    @Size(min=1, max =75)
    Set<Long> organizationIds;
    @NotEmpty
    Set<Long> userIds;
}
