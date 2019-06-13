package com.dizzion.portal.domain.helpdesk.dto;

import com.dizzion.portal.domain.organization.dto.ShortOrganizationInfo;
import com.dizzion.portal.domain.user.dto.ShortUserInfo;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Optional;

@Value
@Builder
public class TicketDetails {
    String description;
    List<String> ccEmails;
    List<TicketComment> comments;
    List<TicketAttachment> attachments;
    List<MemberReference> assignedMembers;
    Optional<ShortUserInfo> portalUser;
    Optional<ShortOrganizationInfo> portalOrganization;
}
