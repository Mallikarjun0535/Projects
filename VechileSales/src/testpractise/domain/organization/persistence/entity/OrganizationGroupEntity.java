package com.dizzion.portal.domain.organization.persistence.entity;

import com.dizzion.portal.domain.scope.persistence.TenantResource;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "organization_group")
@TenantResource(readerTenantPath = "owner.tenantPath")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizationGroupEntity {
    @Id
    @GeneratedValue
    private long id;
    private String name;
    @ManyToMany
    @JoinTable(name = "organization_group_organization",
            inverseJoinColumns = @JoinColumn(name = "organization_id"),
            joinColumns = @JoinColumn(name = "organization_group_id"))
    Set<OrganizationEntity> organizations;
    @ManyToOne
    private OrganizationEntity owner;
}