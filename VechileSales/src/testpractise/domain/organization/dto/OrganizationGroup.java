package com.dizzion.portal.domain.organization.dto;

import com.dizzion.portal.domain.common.dto.ShortEntityInfo;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationGroupEntity;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Value
@Builder
public class OrganizationGroup {
    long id;
    String name;
    List<Organization> organizations;
    String ownerTenantPath;
    ShortEntityInfo owner;

    public static OrganizationGroup from(OrganizationGroupEntity entity) {
        return OrganizationGroup.builder()
                .id(entity.getId())
                .name(entity.getName())
                .organizations(entity.getOrganizations().stream()
                        .map(Organization::from)
                        .collect(toList()))
                .ownerTenantPath(entity.getOwner().getTenantPath())
                .owner(new ShortEntityInfo(entity.getOwner().getId(), entity.getOwner().getName()))
                .build();
    }
}