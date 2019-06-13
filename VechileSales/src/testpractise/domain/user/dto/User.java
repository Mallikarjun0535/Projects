package com.dizzion.portal.domain.user.dto;

import com.dizzion.portal.domain.organization.dto.Organization;
import com.dizzion.portal.domain.organization.dto.Organization.Feature;
import com.dizzion.portal.domain.role.Permission;
import com.dizzion.portal.domain.role.dto.Role;
import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSet;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;
import java.util.Set;

import static com.dizzion.portal.domain.organization.dto.Organization.OrganizationType.PORTAL_ADMIN;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.CollectionUtils.isEmpty;

@Value
@Builder
public class User {
    Long id;
    String email;
    String firstName;
    String lastName;
    Optional<String> mobilePhoneNumber;
    Optional<String> workPhoneNumber;
    int pin;
    Organization organization;
    Role role;
    Set<NotificationMethod> notificationMethods;
    boolean memberOfDizzionTeams;

    public static User from(UserEntity entity) {
        Organization org = Organization.from(entity.getOrganization());
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .notificationMethods(entity.getNotificationMethods())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .mobilePhoneNumber(entity.getMobilePhoneNumber())
                .workPhoneNumber(entity.getWorkPhoneNumber())
                .organization(org)
                .role(Role.from(entity.getRole()))
                .memberOfDizzionTeams(!isEmpty(entity.getDizzionTeams()))
                .pin(entity.getPin())
                .build();
    }

    public Set<String> getPermissions() {
        return ImmutableSet.<String>builder()
                .addAll(role.getPermissions().stream().map(Permission::toString).collect(toSet()))
                .addAll(organization.getFeatures().stream().map(Feature::asPermission).collect(toSet()))
                .build();
    }

    public String toNameEmailString() {
        return getFirstName() + " " + getLastName() + " <" + getEmail() + ">";
    }

    public String toNameEmailOrganizationString() {
        return toNameEmailString() + " from " + organization.getName();
    }

    public enum NotificationMethod {
        EMAIL, SMS
    }

    @JsonIgnore
    public boolean isPortalAdmin() {
        return organization.getType() == PORTAL_ADMIN;
    }
}
