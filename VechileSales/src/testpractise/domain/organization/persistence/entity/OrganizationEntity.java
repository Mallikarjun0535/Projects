package com.dizzion.portal.domain.organization.persistence.entity;

import com.dizzion.portal.domain.application.persistence.entity.ApplicationEntity;
import com.dizzion.portal.domain.application.persistence.entity.ApplicationGroupEntity;
import com.dizzion.portal.domain.filter.NonFilterable;
import com.dizzion.portal.domain.organization.dto.Organization.Feature;
import com.dizzion.portal.domain.organization.dto.Organization.OrganizationType;
import com.dizzion.portal.domain.scope.persistence.TenantResource;
import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import lombok.*;

import javax.persistence.*;
import java.util.Optional;
import java.util.Set;

import static javax.persistence.EnumType.STRING;

@Entity
@Table(name = "organization")
@TenantResource(writerTenantPath = "tenantPath")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationEntity {
    @Id
    @GeneratedValue
    private long id;
    private String customerId;
    private String name;
    private String tenantPath;
    @ManyToOne
    private OrganizationEntity parent;
    @Enumerated(STRING)
    private OrganizationType type;
    private boolean enabled;
    private boolean compliant;
    private boolean twoFactorAuth;
    @ManyToMany(mappedBy = "organizations")
    private Set<ApplicationGroupEntity> applicationGroups;
    @NonFilterable
    @ElementCollection
    @CollectionTable(name = "organization_feature", joinColumns = @JoinColumn(name = "organization_id"))
    @Column(name = "feature")
    @Enumerated(STRING)
    private Set<Feature> features;
    private String supportPhoneNumber;
    @ManyToOne
    private UserEntity customerRelationshipManager;
    @ManyToOne
    private UserEntity serviceDeliveryManager;
    @ManyToMany
    @JoinTable(name = "organization_starred_apps",
            inverseJoinColumns = @JoinColumn(name = "application_id"),
            joinColumns = @JoinColumn(name = "organization_id"))
    private Set<ApplicationEntity> starredApplications;

    public Optional<OrganizationEntity> getParent() {
        return Optional.ofNullable(parent);
    }

    public Optional<UserEntity> getCustomerRelationshipManager() {
        return Optional.ofNullable(customerRelationshipManager);
    }

    public Optional<UserEntity> getServiceDeliveryManager() {
        return Optional.ofNullable(serviceDeliveryManager);
    }
}
