package com.dizzion.portal.domain.helpdesk.dto;

import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.Optional;

@Value
@Builder
public class ConnectWiseTicketNote {
    long id;
    String text;
    boolean detailDescriptionFlag;
    boolean internalAnalysisFlag;
    boolean resolutionFlag;
    Optional<NamedReference> member;
    Optional<NamedReference> contact;
    String createdBy;
    ZonedDateTime dateCreated;
}
