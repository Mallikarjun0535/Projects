package com.dizzion.portal.domain.helpdesk.dto;

import com.dizzion.portal.domain.helpdesk.persistence.entity.OrganizationTemperatureChangeEntity;
import com.dizzion.portal.domain.user.dto.ShortUserInfo;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;

@Value
@Builder
public class OrganizationTemperatureChange {
    private Temperature temperature;
    private String comment;
    private ZonedDateTime timestamp;
    private ShortUserInfo user;

    public static OrganizationTemperatureChange from(OrganizationTemperatureChangeEntity entity) {
        return OrganizationTemperatureChange.builder()
                .temperature(entity.getTemperature())
                .comment(entity.getComment())
                .timestamp(entity.getTimestamp())
                .user(ShortUserInfo.from(entity.getUser()))
                .build();
    }

    public enum Temperature {
        GREEN,
        YELLOW,
        RED
    }
}
