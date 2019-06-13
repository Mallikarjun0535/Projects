package com.dizzion.portal.domain.notification.persistence.entity;

import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.scope.persistence.TenantResource;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "notification")
@TenantResource(readerTenantPath = "organizations.tenantPath")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationEntity {
    @Id
    @GeneratedValue
    private long id;
    private String title;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String message;
    private LocalDate startDate;
    private LocalDate endDate;
    @ManyToMany
    @JoinTable(name = "notification_organization",
            inverseJoinColumns = @JoinColumn(name = "organization_id"),
            joinColumns = @JoinColumn(name = "notification_id"))
    private Set<OrganizationEntity> organizations;
    @ManyToOne
    private OrganizationEntity owner;
}
