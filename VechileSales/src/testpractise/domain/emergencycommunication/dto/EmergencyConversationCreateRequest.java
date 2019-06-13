package com.dizzion.portal.domain.emergencycommunication.dto;

import com.dizzion.portal.domain.emergencycommunication.dto.EmergencyConversation.ConversationType;
import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Value
@Builder
public class EmergencyConversationCreateRequest {
    @NotNull
    ConversationType type;
    @NotBlank
    String name;
}
