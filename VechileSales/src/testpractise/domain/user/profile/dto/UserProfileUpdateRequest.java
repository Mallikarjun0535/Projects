package com.dizzion.portal.domain.user.profile.dto;

import com.dizzion.portal.domain.user.dto.User.NotificationMethod;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.Set;

@Value
@ToString(exclude = {"email", "oldPassword", "newPassword"})
public class UserProfileUpdateRequest {
    Optional<String> oldPassword;
    Optional<String> newPassword;

    @NotBlank
    String firstName;
    @NotBlank
    String lastName;
    Optional<String> mobilePhoneNumber;
    Optional<String> workPhoneNumber;
    @Email
    @NotBlank
    String email;
    @NotNull
    @Max(999999)
    Integer pin;
    @NotNull
    Set<NotificationMethod> notificationMethods;

    @Builder
    public UserProfileUpdateRequest(String oldPassword,
                                    String newPassword,
                                    String firstName,
                                    String lastName,
                                    String mobilePhoneNumber,
                                    String workPhoneNumber,
                                    String email,
                                    Integer pin,
                                    Set<NotificationMethod> notificationMethod) {
        this.oldPassword = Optional.ofNullable(oldPassword);
        this.newPassword = Optional.ofNullable(newPassword);
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobilePhoneNumber = Optional.ofNullable(mobilePhoneNumber);
        this.workPhoneNumber = Optional.ofNullable(workPhoneNumber);
        this.email = email;
        this.pin = pin;
        this.notificationMethods = notificationMethod;
    }
}
