package com.dizzion.portal.security.auth.token;


import com.dizzion.portal.domain.dizzionteam.DizzionTeamService;
import com.dizzion.portal.domain.dizzionteam.dto.DizzionTeam;
import com.dizzion.portal.domain.organization.OrganizationService;
import com.dizzion.portal.domain.organization.dto.Organization;
import com.dizzion.portal.domain.role.RoleService;
import com.dizzion.portal.domain.role.dto.Role;
import com.dizzion.portal.domain.user.UserService;
import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import com.dizzion.portal.security.auth.JwtService;
import com.dizzion.portal.security.auth.Token;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.dizzion.portal.domain.role.dto.Role.BUSINESS;
import static java.util.stream.Collectors.toSet;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class TokenService {

    private final UserService userService;
    private final OrganizationService organizationService;
    private final RoleService roleService;
    private final JwtService jwtService;
    private final AuthenticatedUserAccessor auth;
    private final DizzionTeamService dizzionTeamService;
    private final TwoFactorAuthService twoFactorAuthService;

    public CompletableFuture<String> generateToken(AuthCredentials credentials) {
        User user = userService.getByEmailAndPassword(credentials.getEmail(), credentials.getPassword())
                .orElseThrow(() -> new BadCredentialsException(""));
        if (!user.getOrganization().isEnabled()) {
            throw new DisabledException("");
        }
        return twoFactorAuthService.secondFactorAuthenticate(user, credentials.getTwoFactorAuthToken())
                .thenApply((v) -> jwtService.generate(Token.from(user)));
    }

    public String generateExternalToken(long userId) {
        User user = userService.getUser(userId);
        return jwtService.generateExternalToken(Token.from(user));
    }

    public String generateUnscopedToken() {
        User originalUser = userService.getUser(auth.getAuthenticatedUser().getUser().getId());
        return jwtService.generate(Token.from(originalUser));
    }

    public String generateScopedToken(ScopeChangeRequest scopeChangeRequest) {
        User user = auth.getAuthenticatedUser().getUser();
        Organization scopeOrg = organizationService.getOrganization(scopeChangeRequest.getOrganizationId());
        Role scopeRole = roleService.getRole(scopeChangeRequest.getRole());

        if (user.getRole().getName().equals(BUSINESS)) {
            return jwtService.generate(Token.from(user, scopeOrg, user.getRole()));
        }

        Set<String> rolesAvailableForScopeOrg = roleService.getRolesAvailableForOrganizationType(
                scopeRole.getName(),
                scopeOrg.getType())
                .stream()
                .map(Role::getName)
                .collect(toSet());

        if (rolesAvailableForScopeOrg.contains(scopeRole.getName())) {
            return jwtService.generate(Token.from(user, scopeOrg, scopeRole));
        } else {
            throw new IllegalArgumentException("Unavailable role: " + scopeRole.getName());
        }
    }

    public String generateScopedTokenForDizzionTeam(ScopeChangeRequest scopeChangeRequest) {
        User user = auth.getAuthenticatedUser().getUser();
        Organization scopeOrg = organizationService.getOrganization(scopeChangeRequest.getOrganizationId());

        Set<DizzionTeam> dizzionTeams = dizzionTeamService.getDizzionTeamsByUser(user.getId());
        dizzionTeams.stream()
                .flatMap(dizzionTeam -> dizzionTeam.getOrganizations().stream())
                .filter(organization -> organization.getId().equals(scopeChangeRequest.getOrganizationId()))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);

        return jwtService.generate(Token.from(user, scopeOrg, user.getRole()));
    }
}
