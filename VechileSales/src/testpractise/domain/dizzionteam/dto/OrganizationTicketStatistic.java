package com.dizzion.portal.domain.dizzionteam.dto;

import com.dizzion.portal.domain.helpdesk.dto.OrganizationTemperatureChange.Temperature;
import com.dizzion.portal.domain.organization.dto.Organization;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrganizationTicketStatistic {
    Organization organization;
    Integer openTickets;
    Integer severityMediumTickets;
    Integer severityHighTickets;
    Temperature temperature;
}
