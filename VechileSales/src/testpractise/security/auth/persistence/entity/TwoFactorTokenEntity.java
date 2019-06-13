package com.dizzion.portal.security.auth.persistence.entity;

import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "user_two_factor_token")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TwoFactorTokenEntity {
    @Id
    @GeneratedValue
    private long id;
    private int token;
    @OneToOne
    private UserEntity user;
}
