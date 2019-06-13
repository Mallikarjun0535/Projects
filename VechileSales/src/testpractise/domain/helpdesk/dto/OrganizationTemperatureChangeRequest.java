package com.dizzion.portal.domain.helpdesk.dto;

import com.dizzion.portal.domain.helpdesk.dto.OrganizationTemperatureChange.Temperature;
import lombok.Value;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Value
public class OrganizationTemperatureChangeRequest {
    @NotNull
    Temperature temperature;
    @NotBlank
    String comment;
}
