package com.dizzion.portal.domain.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

@Value
public class NamedReference {
    long id;
    String name;

    @JsonCreator
    public NamedReference(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public NamedReference(long id) {
        this.id = id;
        this.name = null;
    }
}
