package com.dizzion.portal.domain.helpdesk.dto;

import com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Impact;
import com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Severity;
import com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Status;
import com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Type;
import lombok.Builder;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Value
public class TicketCreateRequest {
    @NotBlank
    String summary;
    @NotNull
    Severity severity;
    @NotNull
    Impact impact;
    @NotNull
    Type type;
    Optional<String> detailDescription;
    Optional<String> contactEmail;
    Set<String> ccEmails;
    Optional<Status> status;
    Optional<Long> organizationId;
    Optional<Long> boardId;
    NamedReference priority;

    @Builder
    public TicketCreateRequest(String summary, Severity severity, Impact impact, Type type, String detailDescription, Set<String> ccEmails,
                               Status status, Long organizationId, Long boardId, String contactEmail, NamedReference priority) {
        this.summary = summary;
        this.severity = severity;
        this.impact = impact;
        this.type = type;
        this.detailDescription = Optional.ofNullable(detailDescription);
        this.contactEmail = Optional.ofNullable(contactEmail);
        this.ccEmails = Optional.ofNullable(ccEmails).orElse(new HashSet<>());
        this.status = Optional.ofNullable(status);
        this.organizationId = Optional.ofNullable(organizationId);
        this.boardId = Optional.ofNullable(boardId);
        this.priority = priority;
    }
}
