package com.dizzion.portal.domain.role.persistence.entity;

import com.dizzion.portal.domain.filter.NonFilterable;
import com.dizzion.portal.domain.organization.dto.Organization.OrganizationType;
import com.dizzion.portal.domain.role.Permission;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

import static javax.persistence.EnumType.STRING;

@Entity
@Table(name = "role")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleEntity {
    @Id
    @GeneratedValue
    private long id;
    private String name;
    @NonFilterable
    @ElementCollection
    @CollectionTable(name = "permission", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "permission")
    @Enumerated(STRING)
    private Set<Permission> permissions;
    @NonFilterable
    @ElementCollection
    @CollectionTable(name = "role_organization_type", joinColumns = @JoinColumn(name = "role_id"))
    @Column(name = "organization_type")
    @Enumerated(STRING)
    private Set<OrganizationType> organizationTypes;
}
