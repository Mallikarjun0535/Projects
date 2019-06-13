package com.dizzion.portal.domain.organization.dto;

import com.dizzion.portal.domain.common.dto.ShortEntityInfo;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.role.Permission;
import com.dizzion.portal.domain.user.dto.User;
import com.google.common.collect.ImmutableSet;
import lombok.Builder;
import lombok.Value;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.dizzion.portal.domain.organization.dto.Organization.Feature.EDIT_TICKETS;
import static com.dizzion.portal.domain.organization.dto.Organization.Feature.VIEW_TICKETS;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

@Value
@Builder
public class Organization {
    Long id;
    String customerId;
    String name;
    String tenantPath;
    Optional<ShortEntityInfo> parentInfo;
    OrganizationType type;
    boolean enabled;
    boolean compliant;
    boolean twoFactorAuth;
    Set<Feature> features;
    Ticketing ticketing;
    String supportPhoneNumber;
    Optional<User> customerRelationshipManager;
    Optional<User> serviceDeliveryManager;
    Set<ShortEntityInfo> starredApplications;

    public static Organization from(OrganizationEntity entity) {
        return Organization.builder()
                .id(entity.getId())
                .customerId(entity.getCustomerId())
                .name(entity.getName())
                .tenantPath(entity.getTenantPath())
                .parentInfo(entity.getParent().map(parent -> new ShortEntityInfo(parent.getId(), parent.getName())))
                .type(entity.getType())
                .enabled(entity.isEnabled())
                .compliant(entity.isCompliant())
                .twoFactorAuth(entity.isTwoFactorAuth())
                .features(new HashSet<>(entity.getFeatures()))
                .ticketing(Ticketing.fromFeatures(entity.getFeatures()))
                .supportPhoneNumber(entity.getSupportPhoneNumber())
                .customerRelationshipManager(entity.getCustomerRelationshipManager().map(User::from))
                .serviceDeliveryManager(entity.getServiceDeliveryManager().map(User::from))
                .starredApplications(entity.getStarredApplications().stream()
                        .map(app -> new ShortEntityInfo(app.getId(), app.getName()))
                        .collect(toSet()))
                .build();
    }

    public enum OrganizationType {
        CUSTOMER(),
        PARTNER(CUSTOMER),
        DIZZION(PARTNER, CUSTOMER),
        PORTAL_ADMIN(PARTNER, CUSTOMER);

        private final Set<OrganizationType> availableTypes;

        OrganizationType(OrganizationType... availableTypes) {
            this.availableTypes = ImmutableSet.copyOf(availableTypes);
        }

        public Set<OrganizationType> getAvailableTypes() {
            return availableTypes;
        }
    }

    public enum Feature {
        VIEW_TICKETS(Permission.Constants.VIEW_TICKETS),
        EDIT_TICKETS(Permission.Constants.EDIT_TICKETS);

        private String permission;

        Feature(String permission) {
            this.permission = permission;
        }

        public String asPermission() {
            return permission;
        }
    }

    public enum Ticketing {
        ENABLED(VIEW_TICKETS, EDIT_TICKETS),
        READONLY(VIEW_TICKETS),
        DISABLED();

        private Set<Feature> features;

        Ticketing(Feature... features) {
            this.features = newHashSet(features);
        }

        public Set<Feature> asFeatures() {
            return features;
        }

        public static Ticketing fromFeatures(Collection<Feature> features) {
            return features.containsAll(asList(VIEW_TICKETS, EDIT_TICKETS))
                    ? ENABLED
                    : features.contains(VIEW_TICKETS)
                    ? READONLY
                    : DISABLED;
        }
    }
}