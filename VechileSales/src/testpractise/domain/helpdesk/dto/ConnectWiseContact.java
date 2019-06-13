package com.dizzion.portal.domain.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;
import java.util.Set;

import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseContact.CommunicationItem.Type.EMAIL;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseContact.CommunicationItem.Type.PHONE;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseContact.CommunicationItem.emailCom;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseContact.CommunicationItem.phoneCom;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Sets.newHashSet;

@Value
public class ConnectWiseContact {
    long id;
    String firstName;
    String lastName;
    CompanyReference company;
    Set<CommunicationItem> communicationItems;

    @JsonCreator
    public ConnectWiseContact(long id, String firstName, String lastName, CompanyReference company, Set<CommunicationItem> communicationItems) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.company = company;
        this.communicationItems = communicationItems;
    }

    @Builder
    public ConnectWiseContact(long id, String firstName, String lastName, String companyCid, String email, String phone) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.company = new CompanyReference(companyCid);
        this.communicationItems = newHashSet(emailCom(email));
        if (phone != null) {
            this.communicationItems.add(phoneCom(phone));
        }
    }

    @JsonIgnore
    public String getEmail() {
        return tryFind(communicationItems, com -> com.getType().equals(EMAIL)).toJavaUtil()
                .map(CommunicationItem::getValue)
                .orElseThrow(() -> new IllegalStateException("ConnectWise contact without an email"));
    }

    @JsonIgnore
    public Optional<String> getPhone() {
        return tryFind(communicationItems, com -> com.getType().equals(PHONE)).toJavaUtil()
                .map(CommunicationItem::getValue);
    }


    @Value
    public static class CommunicationItem {
        Type type;
        String value;
        boolean defaultFlag = true;

        public static CommunicationItem emailCom(String email) {
            return new CommunicationItem(EMAIL, email);
        }

        public static CommunicationItem phoneCom(String phone) {
            return new CommunicationItem(PHONE, phone);
        }

        @Value
        public static class Type {
            public static final Type EMAIL = new Type(1, "Email");
            public static final Type PHONE = new Type(2, "Direct");

            long id;
            String name;
        }
    }
}
