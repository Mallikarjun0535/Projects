package com.dizzion.portal.domain.user.registration.dto;

import lombok.ToString;
import lombok.Value;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

@Value
@ToString(exclude = {"password"})
public class PhoneNumberWithCredentials {
    @NotBlank
    String phoneNumber;
    @Email
    @NotBlank
    String email;
    @NotBlank
    String password;
}
