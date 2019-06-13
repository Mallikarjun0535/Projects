package com.dizzion.portal.security.auth.token;

import lombok.ToString;
import lombok.Value;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

@Value
@ToString(exclude = {"password"})
public class EmailAndPassword {
    @Email
    @NotBlank
    public String email;
    @NotBlank
    public String password;
}
