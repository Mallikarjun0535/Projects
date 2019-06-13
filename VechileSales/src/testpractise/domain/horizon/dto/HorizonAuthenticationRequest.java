package com.dizzion.portal.domain.horizon.dto;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;

@Value
@Builder
@ToString(exclude = {"password"})
public class HorizonAuthenticationRequest {
    @NotBlank
    String applicationUrl;
    @NotBlank
    String username;
    @NotBlank
    String password;
    @NotBlank
    String domain;
}
