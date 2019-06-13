package com.dizzion.portal.domain.helpdesk.dto;

import lombok.Builder;
import lombok.Value;

import java.util.Optional;

@Value
@Builder
public class Contact {
    long id;
    String firstName;
    String lastName;
    String email;
    Optional<String> phone;
    String companyName;

    public static Contact from(ConnectWiseContact contact) {
        return Contact.builder()
                .id(contact.getId())
                .firstName(contact.getFirstName())
                .lastName(contact.getLastName())
                .companyName(contact.getCompany().getName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .build();
    }
}
