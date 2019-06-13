package com.dizzion.portal.domain.user.registration.dto;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Builder
@Value
@ToString(exclude = {"password", "secret", "pin"})
public class RegistrationRequest {
    /*
        ^                 # start-of-string
        (?=.*[0-9])       # a digit must occur at least once
        (?=.*[a-z])       # a lower case letter must occur at least once
        (?=.*[A-Z])       # an upper case letter must occur at least once
        .{6,}             # anything, at least six places though
        $                 # end-of-string
     */
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,}$",
            message = "must be at least 6 characters long, be of mixed case and contain a digit")
    @NotBlank
    String password;
    @NotBlank
    String secret;
    @NotNull
    @Max(999999)
    Integer pin;
    @NotBlank
    String phoneNumber;
}
