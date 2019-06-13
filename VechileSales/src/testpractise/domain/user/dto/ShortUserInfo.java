package com.dizzion.portal.domain.user.dto;

import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;

@Value
@Builder
public class ShortUserInfo {
    long id;
    String customerId;
    String firstName;
    String lastName;
    String email;
    Optional<String> mobilePhone;
    Optional<String> workPhone;

    public static ShortUserInfo from(UserEntity entity) {
        return builder()
                .id(entity.getId())
                .customerId(entity.getOrganization().getCustomerId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .mobilePhone(entity.getMobilePhoneNumber())
                .workPhone(entity.getWorkPhoneNumber())
                .build();
    }

    public static ShortUserInfo from(User user) {
        return builder()
                .id(user.getId())
                .customerId(user.getOrganization().getCustomerId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .mobilePhone(user.getMobilePhoneNumber())
                .workPhone(user.getWorkPhoneNumber())
                .build();
    }
}
