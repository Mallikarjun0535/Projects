package com.dizzion.portal.domain.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

@Value
public class MemberReference {
    String identifier;
    String name;

    @JsonCreator
    public MemberReference(String identifier, String name) {
        this.identifier = identifier;
        this.name = name;
    }

    public MemberReference(String identifier) {
        this.identifier = identifier;
        this.name = null;
    }

    public static MemberReference from(ConnectWiseMember member) {
        return new MemberReference(member.getIdentifier(), member.getFirstName() + " " + member.getLastName());
    }
}
