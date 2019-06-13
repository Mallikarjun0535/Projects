package com.dizzion.portal.domain.application.dto;

import com.dizzion.portal.domain.application.persistence.entity.ApplicationGroupEntity;
import com.dizzion.portal.domain.common.dto.ShortEntityInfo;
import com.dizzion.portal.domain.organization.dto.Organization;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

@Value
@Builder
public class ApplicationGroup {
    long id;
    String name;
    boolean enabled;
    List<Organization> organizations;
    String ownerTenantPath;
    ShortEntityInfo owner;

    public static ApplicationGroup from(ApplicationGroupEntity entity) {
        return fromWithOrgFilter(entity, x -> true);
    }

    public static ApplicationGroup fromWithOrgFilter(ApplicationGroupEntity entity, Predicate<Organization> orgFilter) {
        return ApplicationGroup.builder()
                .id(entity.getId())
                .name(entity.getName())
                .enabled(entity.isEnabled())
                .organizations(entity.getOrganizations().stream()
                        .map(Organization::from)
                        .filter(orgFilter)
                        .collect(toList()))
                .ownerTenantPath(entity.getOwner().getTenantPath())
                .owner(new ShortEntityInfo(entity.getOwner().getId(), entity.getOwner().getName()))
                .build();
    }
}