package com.dizzion.portal.domain.maintenance.persistence.entity;

import com.dizzion.portal.domain.scope.persistence.TenantResource;
import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "maintenance_event_approval")
@TenantResource(readerTenantPath = "user.organizations.tenantPath", writerTenantPath = "user.organizations.tenantPath")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaintenanceEventApprovalEntity {
    @Id
    @GeneratedValue
    private long id;
    @ManyToOne
    private MaintenanceEventEntity maintenanceEvent;
    @ManyToOne
    private UserEntity user;
    private boolean approved;
}
