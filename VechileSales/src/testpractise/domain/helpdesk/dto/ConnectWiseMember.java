package com.dizzion.portal.domain.helpdesk.dto;

import com.dizzion.portal.domain.user.dto.ShortUserInfo;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ConnectWiseMember {
    String identifier;
    String firstName;
    String lastName;
    String officeEmail;
    ShortUserInfo portalUser;
}