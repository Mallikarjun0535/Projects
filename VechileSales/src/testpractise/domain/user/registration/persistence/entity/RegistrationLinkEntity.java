package com.dizzion.portal.domain.user.registration.persistence.entity;

import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "user_registration_link")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistrationLinkEntity {
    @Id
    @GeneratedValue
    private long id;
    private String linkSecretPath;
    @OneToOne
    private UserEntity user;
}