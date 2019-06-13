package com.dizzion.portal.domain.emergencycommunication.dto;

import com.dizzion.portal.domain.emergencycommunication.persistence.entity.EmergencyMessageEntity;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;

@Value
@Builder
public class EmergencyMessage {
    Long id;
    Long emergencyConversationId;
    String content;
    ZonedDateTime createdAt;
    boolean internal;

    public static EmergencyMessage from(EmergencyMessageEntity entity) {
        return EmergencyMessage.builder()
                .id(entity.getId())
                .emergencyConversationId(entity.getEmergencyConversationId())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .internal(entity.isInternal())
                .build();
    }
}
