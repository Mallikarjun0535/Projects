package com.dizzion.portal.domain.organization.dto;

import com.dizzion.portal.domain.user.dto.ShortUserInfo;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;

@Value
@Builder
public class SupportContacts {
    String supportPhoneNumber;
    Optional<ShortUserInfo> customerRelationshipManager;
    Optional<ShortUserInfo> serviceDeliveryManager;
}
