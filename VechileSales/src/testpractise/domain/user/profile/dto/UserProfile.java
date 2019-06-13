package com.dizzion.portal.domain.user.profile.dto;

import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.domain.user.dto.User.NotificationMethod;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;
import java.util.Set;

@Value
@Builder
public class UserProfile {
    String firstName;
    String lastName;
    Optional<String> mobilePhoneNumber;
    Optional<String> workPhoneNumber;
    int pin;
    String email;
    String organizationName;
    Set<NotificationMethod> notificationMethods;

    public static UserProfile from(User user) {
        return UserProfile.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .mobilePhoneNumber(user.getMobilePhoneNumber())
                .workPhoneNumber(user.getWorkPhoneNumber())
                .email(user.getEmail())
                .organizationName(user.getOrganization().getName())
                .notificationMethods(user.getNotificationMethods())
                .pin(user.getPin())
                .build();
    }
}
