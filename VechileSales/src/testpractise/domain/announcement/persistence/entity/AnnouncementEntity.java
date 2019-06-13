package com.dizzion.portal.domain.announcement.persistence.entity;

import com.dizzion.portal.domain.filter.NonFilterable;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.scope.persistence.TenantResource;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "announcement")
@TenantResource(readerTenantPath = "organizations.tenantPath")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnnouncementEntity {

    @Id
    @GeneratedValue
    private long id;
    private String title;
    @NonFilterable
    @Type(type = "com.dizzion.portal.config.hibernate.json.JsonUserType")
    private List<String> pages;
    private LocalDate startDate;
    private LocalDate endDate;
    @ManyToMany
    @JoinTable(name = "announcement_organization",
            inverseJoinColumns = @JoinColumn(name = "organization_id"),
            joinColumns = @JoinColumn(name = "announcement_id"))
    private Set<OrganizationEntity> organizations;
    @ManyToOne
    private OrganizationEntity owner;
}
