package com.dizzion.portal.security.auth.token;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Optional;

@Value
@ToString(exclude = {"password", "twoFactorAuthToken"})
public class AuthCredentials {
    @Email
    @NotBlank
    String email;
    @NotBlank
    String password;
    Optional<Integer> twoFactorAuthToken;

    @Builder
    public AuthCredentials(String email, String password, Integer twoFactorAuthToken) {
        this.email = email;
        this.password = password;
        this.twoFactorAuthToken = Optional.ofNullable(twoFactorAuthToken);
    }
}
