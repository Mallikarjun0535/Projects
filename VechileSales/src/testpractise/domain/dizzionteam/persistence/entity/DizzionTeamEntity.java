package com.dizzion.portal.domain.dizzionteam.persistence.entity;

import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "dizzion_team")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DizzionTeamEntity {
    @Id
    @GeneratedValue
    private long id;
    private String name;
    @ManyToMany
    @JoinTable(name = "dizzion_team_organization",
            inverseJoinColumns = @JoinColumn(name = "organization_id"),
            joinColumns = @JoinColumn(name = "dizzion_team_id"))
    Set<OrganizationEntity> organizations;
    @ManyToMany
    @JoinTable(name = "dizzion_team_user",
            inverseJoinColumns = @JoinColumn(name = "user_id"),
            joinColumns = @JoinColumn(name = "dizzion_team_id"))
    Set<UserEntity> users;
}
