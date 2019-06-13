package com.dizzion.portal.security.auth.spring;

import com.dizzion.portal.domain.organization.dto.Organization;
import com.dizzion.portal.domain.organization.persistence.OrganizationRepository;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.role.dto.Role;
import com.dizzion.portal.domain.role.persistence.RoleRepository;
import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.domain.user.persistence.UserRepository;
import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import com.dizzion.portal.security.auth.JwtService;
import com.dizzion.portal.security.auth.Token;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final OrganizationRepository orgRepo;
    private final RoleRepository roleRepo;

    public JwtAuthenticationProvider(JwtService jwtService, UserRepository userRepo, OrganizationRepository orgRepo, RoleRepository roleRepo) {
        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.orgRepo = orgRepo;
        this.roleRepo = roleRepo;
    }

    @Transactional(readOnly = true)
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwtAuthentication jwtAuthentication = (JwtAuthentication) authentication;
        Token token = jwtService.parse(jwtAuthentication.getToken());
        boolean noscope = jwtAuthentication.isNoscope();

        UserEntity userEntity = userRepo.findOne(token.getUserId());
        if (userEntity == null) {
            throw new AuthenticationCredentialsNotFoundException("Cannot find user with userId=" + token.getUserId());
        }
        if (!userEntity.getOrganization().isEnabled()) {
            throw new DisabledException("Organization with orgId= " + userEntity.getOrganization().getId() + " is disabled");
        }
        return token.getScope() == null || noscope
                ? new AuthenticatedUser(User.from(userEntity), false)
                : new AuthenticatedUser(scopedUser(userEntity, token.getScope()), true);
    }

    private User scopedUser(UserEntity userEntity, Token scope) {
        OrganizationEntity scopeOrg = orgRepo.findOne(scope.getOrganizationId());
        if (!scopeOrg.isEnabled()) {
            throw new DisabledException("Organization with orgId= " + scopeOrg.getId() + " is disabled");
        }
        return User.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .mobilePhoneNumber(userEntity.getMobilePhoneNumber())
                .workPhoneNumber(userEntity.getWorkPhoneNumber())
                .role(Role.from(roleRepo.findByName(scope.getRole())))
                .organization(Organization.from(scopeOrg))
                .build();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthentication.class.isAssignableFrom(authentication);
    }
}
