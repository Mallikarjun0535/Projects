package com.dizzion.portal.domain.user.dto;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Value
@ToString(exclude = {"email"})
public class UserCreateUpdateRequest {
    @Email
    @NotBlank
    String email;
    @NotBlank
    String firstName;
    @NotBlank
    String lastName;
    Optional<String> mobilePhoneNumber;
    Optional<String> workPhoneNumber;
    @NotBlank
    String role;
    @NotNull
    Long organizationId;

    @Builder
    public UserCreateUpdateRequest(String email,
                                   String firstName,
                                   String lastName,
                                   String mobilePhoneNumber,
                                   String workPhoneNumber,
                                   String role,
                                   Long organizationId) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobilePhoneNumber = Optional.ofNullable(mobilePhoneNumber);
        this.workPhoneNumber = Optional.ofNullable(workPhoneNumber);
        this.role = role;
        this.organizationId = organizationId;
    }
}
