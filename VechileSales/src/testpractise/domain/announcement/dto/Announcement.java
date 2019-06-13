package com.dizzion.portal.domain.announcement.dto;

import com.dizzion.portal.domain.announcement.persistence.entity.AnnouncementEntity;
import com.dizzion.portal.domain.organization.dto.Organization;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Value
@Builder
public class Announcement {
    Long id;
    String title;
    String[] pages;
    LocalDate startDate;
    LocalDate endDate;
    Set<Organization> organizations;
    String ownerTenantPath;

    public static Announcement from(AnnouncementEntity entity) {

        return Announcement.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .pages(entity.getPages().stream().toArray(String[]::new))
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .organizations(entity.getOrganizations().stream().map(Organization::from).collect(toSet()))
                .ownerTenantPath(entity.getOwner().getTenantPath())
                .build();
    }
}