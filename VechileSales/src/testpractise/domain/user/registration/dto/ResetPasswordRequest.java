package com.dizzion.portal.domain.user.registration.dto;

import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;

@Value
public class ResetPasswordRequest {
    @NotBlank
    String email;
}
