package com.dizzion.portal.domain.organization.dto;

import com.dizzion.portal.domain.organization.dto.Organization.Ticketing;
import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Value
public class OrganizationUpdateRequest {
    @NotBlank
    String customerId;
    @NotBlank
    String name;
    @NotNull
    Boolean enabled;
    @NotNull
    Boolean compliant;
    @NotNull
    Boolean twoFactorAuth;
    @NotNull
    Ticketing ticketing;
    Optional<String> supportPhoneNumber;
    Optional<Long> customerRelationshipManagerId;
    Optional<Long> serviceDeliveryManagerId;
    Set<Long> starredApplicationIds;

    @Builder
    public OrganizationUpdateRequest(String customerId, String name, Boolean enabled, Ticketing ticketing, String supportPhoneNumber,
                                     Long customerRelationshipManagerId, Long serviceDeliveryManagerId, Set<Long> starredApplicationIds,
                                     Boolean compliant, Boolean twoFactorAuth) {
        this.customerId = customerId;
        this.name = name;
        this.enabled = enabled;
        this.compliant = compliant;
        this.twoFactorAuth = twoFactorAuth;
        this.ticketing = ticketing;
        this.supportPhoneNumber = Optional.ofNullable(supportPhoneNumber);
        this.customerRelationshipManagerId = Optional.ofNullable(customerRelationshipManagerId);
        this.serviceDeliveryManagerId = Optional.ofNullable(serviceDeliveryManagerId);
        this.starredApplicationIds = Optional.ofNullable(starredApplicationIds).orElse(new HashSet<>());
    }
}
