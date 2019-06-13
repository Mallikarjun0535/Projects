package com.dizzion.portal.security.auth.token;

import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Value
public class ScopeChangeRequest {
    @NotNull
    Long organizationId;
    @NotBlank
    String role;
}
