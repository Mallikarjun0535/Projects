package com.dizzion.portal.domain.application.dto;

import com.dizzion.portal.domain.application.persistence.entity.ApplicationEntity;
import com.dizzion.portal.domain.common.dto.ShortEntityInfo;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Value
@Builder
public class Application {
    Long id;
    String name;
    String description;
    String url;
    boolean horizon;
    Set<ApplicationGroup> applicationGroups;
    String ownerTenantPath;
    ShortEntityInfo owner;
    boolean starred;

    public static Application from(ApplicationEntity entity) {
        return prebuild(entity).starred(false).build();
    }

    public static Application starredFrom(ApplicationEntity entity) {
        return prebuild(entity).starred(true).build();
    }

    private static ApplicationBuilder prebuild(ApplicationEntity entity) {
        return Application.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .url(entity.getUrl())
                .applicationGroups(entity.getApplicationGroups().stream()
                        .map(ApplicationGroup::from)
                        .collect(toSet()))
                .horizon(entity.isHorizon())
                .ownerTenantPath(entity.getOwner().getTenantPath())
                .owner(new ShortEntityInfo(entity.getOwner().getId(), entity.getOwner().getName()))
                .starred(false);
    }
}
