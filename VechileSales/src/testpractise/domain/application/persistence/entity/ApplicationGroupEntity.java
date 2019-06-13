package com.dizzion.portal.domain.application.persistence.entity;

import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.scope.persistence.TenantResource;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "application_group")
@TenantResource(readerTenantPath = "organizations.tenantPath")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationGroupEntity {
    @Id
    @GeneratedValue
    private long id;
    private String name;
    private boolean enabled;
    @ManyToMany
    @JoinTable(name = "application_group_organization",
            inverseJoinColumns = @JoinColumn(name = "organization_id"),
            joinColumns = @JoinColumn(name = "application_group_id"))
    Set<OrganizationEntity> organizations;
    @ManyToOne
    private OrganizationEntity owner;
}