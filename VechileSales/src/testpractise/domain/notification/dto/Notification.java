package com.dizzion.portal.domain.notification.dto;

import com.dizzion.portal.domain.common.dto.ShortEntityInfo;
import com.dizzion.portal.domain.notification.persistence.entity.NotificationEntity;
import com.dizzion.portal.domain.organization.dto.Organization;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Value
@Builder
public class Notification {
    Long id;
    String title;
    String message;
    LocalDate startDate;
    LocalDate endDate;
    Set<Organization> organizations;
    String ownerTenantPath;
    ShortEntityInfo owner;

    public static Notification from(NotificationEntity entity) {
        return Notification.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .organizations(entity.getOrganizations().stream().map(Organization::from).collect(toSet()))
                .ownerTenantPath(entity.getOwner().getTenantPath())
                .owner(new ShortEntityInfo(entity.getOwner().getId(), entity.getOwner().getName()))
                .build();
    }
}