package com.dizzion.portal.domain.helpdesk.persistence.entity;

import com.dizzion.portal.domain.helpdesk.dto.OrganizationTemperatureChange.Temperature;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import lombok.*;

import javax.persistence.*;
import java.time.ZonedDateTime;

import static javax.persistence.EnumType.STRING;

@Entity
@Table(name = "organization_temperature_change")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizationTemperatureChangeEntity {
    @Id
    @GeneratedValue
    private long id;
    @ManyToOne
    OrganizationEntity organization;
    @Enumerated(STRING)
    private Temperature temperature;
    private String comment;
    private ZonedDateTime timestamp;
    @ManyToOne
    UserEntity user;
}
