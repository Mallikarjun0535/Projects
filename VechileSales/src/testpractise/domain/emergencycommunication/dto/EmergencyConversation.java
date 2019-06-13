package com.dizzion.portal.domain.emergencycommunication.dto;

import com.dizzion.portal.domain.emergencycommunication.persistence.entity.EmergencyConversationEntity;
import com.dizzion.portal.domain.organization.dto.Organization;
import com.dizzion.portal.domain.organization.dto.OrganizationGroup;
import com.dizzion.portal.infra.template.TemplateService.TextTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.dizzion.portal.infra.template.TemplateService.TextTemplate.*;
import static java.util.stream.Collectors.toSet;

@Value
@Builder
public class EmergencyConversation {
    Long id;
    Set<Organization> recipients;
    Set<OrganizationGroup> recipientGroups;
    ConversationType type;
    Map<String, String> properties;
    String name;
    ZonedDateTime createdAt;
    ZonedDateTime closedAt;
    Optional<Long> helpDeskTicketId;

    public static EmergencyConversation from(EmergencyConversationEntity entity) {
        return EmergencyConversation.builder()
                .id(entity.getId())
                .recipients(entity.getRecipients().stream().map(Organization::from).collect(toSet()))
                .recipientGroups(entity.getRecipientGroups().stream().map(OrganizationGroup::from).collect(toSet()))
                .type(entity.getType())
                .properties(entity.getProperties())
                .name(entity.getName())
                .createdAt(entity.getCreatedAt())
                .closedAt(entity.getClosedAt())
                .helpDeskTicketId(entity.getHelpDeskTicketId())
                .build();
    }

    @Getter
    @AllArgsConstructor
    public enum ConversationType {
        OUTAGE(OUTAGE_OPEN, OUTAGE_UPDATE, OUTAGE_CLOSE),
        DEGRADED_SERVICE(DEGRADED_SERVICE_OPEN, DEGRADED_SERVICE_UPDATE, DEGRADED_SERVICE_CLOSE),
        PATCHING(PATCHING_OPEN, PATCHING_UPDATE, PATCHING_CLOSE),
        GENERAL(GENERAL_OPEN, GENERAL_UPDATE, GENERAL_CLOSE);

        private TextTemplate openMessageTemplate;
        private TextTemplate updateMessageTemplate;
        private TextTemplate closeMessageTemplate;
    }
}
