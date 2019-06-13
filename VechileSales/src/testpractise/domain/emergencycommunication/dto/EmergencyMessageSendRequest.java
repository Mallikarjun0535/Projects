package com.dizzion.portal.domain.emergencycommunication.dto;

import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Value
public class EmergencyMessageSendRequest {
    @NotBlank
    String content;
    @NotNull
    Boolean internal;
}
