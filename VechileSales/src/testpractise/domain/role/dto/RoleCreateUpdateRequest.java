package com.dizzion.portal.domain.role.dto;

import com.dizzion.portal.domain.organization.dto.Organization.OrganizationType;
import com.dizzion.portal.domain.role.Permission;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Set;

@Value
public class RoleCreateUpdateRequest {
    @NotBlank
    String name;
    @NotEmpty
    Set<Permission> permissions;
    @NotEmpty
    Set<OrganizationType> organizationTypes;
}
