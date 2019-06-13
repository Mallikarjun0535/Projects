package com.dizzion.portal.domain.helpdesk.dto;

import com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Impact;
import com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Severity;
import com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Type;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;
import java.util.Set;

import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Status;

@Value
public class TicketUpdateRequest {
    Optional<String> summary;
    Optional<Severity> severity;
    Optional<Impact> impact;
    Optional<Type> type;
    Optional<Status> statusName;
    Optional<Set<String>> ccEmails;
    Optional<String> contactEmail;
    Optional<Long> organizationId;
    Optional<Set<String>> assignedMemberIdentifiers;
    Optional<Long> boardId;
    Optional<NamedReference> priority;

    @Builder
    public TicketUpdateRequest(String summary, Severity severity, Impact impact, Type type, Status statusName, Set<String> ccEmails, String contactEmail, Long organizationId, Set<String> assignedMemberIdentifiers, Long boardId, NamedReference priority) {
        this.summary = Optional.ofNullable(summary);
        this.severity = Optional.ofNullable(severity);
        this.impact = Optional.ofNullable(impact);
        this.type = Optional.ofNullable(type);
        this.statusName = Optional.ofNullable(statusName);
        this.ccEmails = Optional.ofNullable(ccEmails);
        this.contactEmail = Optional.ofNullable(contactEmail);
        this.organizationId = Optional.ofNullable(organizationId);
        this.assignedMemberIdentifiers = Optional.ofNullable(assignedMemberIdentifiers);
        this.boardId = Optional.ofNullable(boardId);
        this.priority = Optional.ofNullable(priority);
    }
}