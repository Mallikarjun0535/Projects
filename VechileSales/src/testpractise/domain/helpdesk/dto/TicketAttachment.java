package com.dizzion.portal.domain.helpdesk.dto;

import lombok.Value;

@Value
public class TicketAttachment {
    long id;
    String fileName;

    public static TicketAttachment from(ConnectWiseDocument doc) {
        return new TicketAttachment(doc.getId(), doc.getFileName());
    }
}
