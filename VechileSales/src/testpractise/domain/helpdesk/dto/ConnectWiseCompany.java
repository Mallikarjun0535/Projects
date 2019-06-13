package com.dizzion.portal.domain.helpdesk.dto;

import lombok.Value;

import java.util.Optional;

@Value
public class ConnectWiseCompany {
    String id;
    String identifier;
    NamedReference defaultContact;

    public Optional<NamedReference> getDefaultContact() {
        return Optional.ofNullable(defaultContact);
    }
}
