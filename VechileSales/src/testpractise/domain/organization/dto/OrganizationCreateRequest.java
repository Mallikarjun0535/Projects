package com.dizzion.portal.domain.organization.dto;

import com.dizzion.portal.domain.organization.dto.Organization.OrganizationType;
import com.dizzion.portal.domain.organization.dto.Organization.Ticketing;
import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Value
public class OrganizationCreateRequest {
    @NotBlank
    String name;
    @NotBlank
    String customerId;
    @NotNull
    Long parentOrganizationId;
    @NotNull
    OrganizationType type;
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
    public OrganizationCreateRequest(String name, String customerId, Long parentOrganizationId, OrganizationType type, Boolean enabled,
                                     Boolean compliant, Boolean twoFactorAuth, Ticketing ticketing, String supportPhoneNumber,
                                     Long customerRelationshipManagerId, Long serviceDeliveryManagerId, Set<Long> starredApplicationIds) {
        this.name = name;
        this.customerId = customerId;
        this.parentOrganizationId = parentOrganizationId;
        this.type = type;
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
