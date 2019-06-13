package com.dizzion.portal.domain.helpdesk.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ConnectWiseScheduleEntry {
    private static final IdReference SERVICE_TYPE_REFERENCE = new IdReference(4);

    Long id;
    Long objectId; // ticket id
    MemberReference member; //FIXME nullable
    IdReference type = SERVICE_TYPE_REFERENCE;
}
