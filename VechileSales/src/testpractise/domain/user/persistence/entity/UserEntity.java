package com.dizzion.portal.domain.user.persistence.entity;

import com.dizzion.portal.domain.application.persistence.entity.ApplicationEntity;
import com.dizzion.portal.domain.dizzionteam.persistence.entity.DizzionTeamEntity;
import com.dizzion.portal.domain.filter.NonFilterable;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.role.persistence.entity.RoleEntity;
import com.dizzion.portal.domain.scope.persistence.TenantResource;
import com.dizzion.portal.domain.user.registration.persistence.entity.RegistrationLinkEntity;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Optional;
import java.util.Set;

import static com.dizzion.portal.domain.user.dto.User.NotificationMethod;
import static org.springframework.util.StringUtils.hasText;

@Entity
@Table(name = "user")
@TenantResource(readerTenantPath = "organization.tenantPath", writerTenantPath = "organization.tenantPath")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity {
    @Id
    @GeneratedValue
    private long id;
    private String password;
    private String email;
    private String firstName;
    private String lastName;
    private String mobilePhoneNumber;
    private String workPhoneNumber;
    private int pin;
    @ManyToOne
    private OrganizationEntity organization;
    @ManyToOne
    private RoleEntity role;
    @OneToOne(mappedBy = "user")
    private RegistrationLinkEntity registrationLink;
    @NonFilterable
    @Type(type = "com.dizzion.portal.config.hibernate.json.JsonUserType")
    private Set<NotificationMethod> notificationMethods;
    @ManyToMany(mappedBy = "users")
    private Set<DizzionTeamEntity> dizzionTeams;
    @ManyToMany
    @JoinTable(name = "user_starred_apps",
            inverseJoinColumns = @JoinColumn(name = "application_id"),
            joinColumns = @JoinColumn(name = "user_id"))
    private Set<ApplicationEntity> starredApplications;

    public Optional<String> getMobilePhoneNumber() {
        return hasText(mobilePhoneNumber) ? Optional.of(mobilePhoneNumber) : Optional.empty();
    }

    public Optional<String> getWorkPhoneNumber() {
        return hasText(workPhoneNumber) ? Optional.of(workPhoneNumber) : Optional.empty();
    }

}
