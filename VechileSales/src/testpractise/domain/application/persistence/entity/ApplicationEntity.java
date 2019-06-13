package com.dizzion.portal.domain.application.persistence.entity;

import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.scope.persistence.TenantResource;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "application")
@TenantResource(readerTenantPath = "applicationGroups.organizations.tenantPath")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationEntity {
    @Id
    @GeneratedValue
    private long id;
    private String name;
    private String description;
    private String url;
    private boolean horizon;
    @ManyToMany
    @JoinTable(name = "application_group_application",
            inverseJoinColumns = @JoinColumn(name = "application_group_id"),
            joinColumns = @JoinColumn(name = "application_id"))
    private Set<ApplicationGroupEntity> applicationGroups;
    @ManyToOne
    private OrganizationEntity owner;
}