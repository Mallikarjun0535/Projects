package com.dizzion.portal.domain.emergencycommunication.dto;

import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;

@Value
@Builder
public class EmergencyConversationUpdateRequest {
    @NotNull
    Set<Long> recipientOrganizationIds;
    @NotNull
    Set<Long> recipientOrganizationGroupIds;
    @NotBlank
    String name;
    @NotNull
    Map<String, String> properties;
}
