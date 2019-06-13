package com.dizzion.portal.domain.maintenance.persistence.entity;

import com.dizzion.portal.domain.scope.persistence.TenantResource;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "maintenance_event_progress_ticket")
@TenantResource(
        readerTenantPath = "progressStatus.maintenanceEvent.user.organizations.tenantPath",
        writerTenantPath = "progressStatus.maintenanceEvent.user.organizations.tenantPath")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
public class MaintenanceEventProgressTicketEntity {
    @Id
    @GeneratedValue
    private long id;
    private long maintenanceEventInstanceId;
    private long organizationId;
    private long ticketId;

    public MaintenanceEventProgressTicketEntity(long maintenanceEventInstanceId, long organizationId, long ticketId) {
        this.maintenanceEventInstanceId = maintenanceEventInstanceId;
        this.organizationId = organizationId;
        this.ticketId = ticketId;
    }
}
