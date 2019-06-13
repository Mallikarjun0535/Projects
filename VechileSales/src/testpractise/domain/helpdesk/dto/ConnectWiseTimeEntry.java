package com.dizzion.portal.domain.helpdesk.dto;

import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;

@Value
@Builder
public class ConnectWiseTimeEntry {
    private final static String DEFAULT_CHARGE_TO_TYPE = "ServiceTicket";

    Long chargeToId; // ticket id
    String chargeToType = DEFAULT_CHARGE_TO_TYPE;
    String notes;
    MemberReference member;
    String timeStart;

    boolean addToDetailDescriptionFlag;
    boolean addToInternalAnalysisFlag;
    boolean addToResolutionFlag;
    boolean emailResourceFlag;
    boolean emailContactFlag;
    boolean emailCcFlag;
    String emailCc;
    ZonedDateTime dateEntered;
}