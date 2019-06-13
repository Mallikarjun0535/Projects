package com.dizzion.portal.domain.maintenance.persistence.entity;

import com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent.ProgressStatus;
import com.dizzion.portal.domain.scope.persistence.TenantResource;
import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import lombok.*;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Optional;

import static javax.persistence.EnumType.STRING;

@Entity
@Table(name = "maintenance_event_instance")
@TenantResource(
        readerTenantPath = "maintenanceEvent.user.organizations.tenantPath",
        writerTenantPath = "maintenanceEvent.user.organizations.tenantPath")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaintenanceEventInstanceEntity {
    @Id
    @GeneratedValue
    private long id;
    @ManyToOne
    private MaintenanceEventEntity maintenanceEvent;
    private ZonedDateTime startDateTime;
    @Enumerated(STRING)
    private ProgressStatus progressStatus;
    @ManyToOne
    private UserEntity assignedUser;
    private boolean removed;

    public Optional<UserEntity> getAssignedUser() {
        return Optional.ofNullable(assignedUser);
    }
}
