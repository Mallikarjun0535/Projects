package com.dizzion.portal.domain.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

@Value
public class CompanyReference {
    String identifier;
    String name;

    @JsonCreator
    public CompanyReference(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    public CompanyReference(String identifier) {
        this.identifier = identifier;
        this.name = null;
    }
}
