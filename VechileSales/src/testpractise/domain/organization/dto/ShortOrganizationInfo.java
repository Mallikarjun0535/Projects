package com.dizzion.portal.domain.organization.dto;

import lombok.Value;

@Value
public class ShortOrganizationInfo {
    long id;
    String customerId;
    String name;

    public static ShortOrganizationInfo from(Organization organization) {
        return new ShortOrganizationInfo(organization.getId(), organization.getCustomerId(), organization.getName());
    }
}
