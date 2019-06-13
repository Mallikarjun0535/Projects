package com.dizzion.portal.domain.role.dto;

import com.dizzion.portal.domain.organization.dto.Organization.OrganizationType;
import com.dizzion.portal.domain.role.Permission;
import com.dizzion.portal.domain.role.persistence.entity.RoleEntity;
import lombok.Value;

import java.util.HashSet;
import java.util.Set;

@Value
public class Role {
    public static final String PORTAL_ADMIN = "Portal Admin";
    public static final String SUPPORT = "Support";
    public static final String PARTNER_ADMIN = "Partner Admin";
    public static final String ORGANIZATION_ADMIN = "Organization Admin";
    public static final String BUSINESS = "Business";
    public static final String USER = "User";
    public static final String ISTONISH = "Istonish";

    Long id;
    String name;
    Set<Permission> permissions;
    Set<OrganizationType> organizationTypes;

    public static Role from(RoleEntity entity) {
        return new Role(
                entity.getId(),
                entity.getName(),
                new HashSet<>(entity.getPermissions()),
                new HashSet<>(entity.getOrganizationTypes()));
    }
}
