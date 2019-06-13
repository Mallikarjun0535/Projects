package com.dizzion.portal.security.auth.spring;

import com.dizzion.portal.domain.user.dto.User;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

public class AuthenticatedUser extends AbstractAuthenticationToken {
    private final User user;
    private final boolean scopeEnabled;

    public AuthenticatedUser(User user, boolean scopeEnabled) {
        super(toAuthorities(user.getPermissions()));
        this.user = user;
        this.scopeEnabled = scopeEnabled;
    }

    @Override
    public Object getPrincipal() {
        return user.getEmail();
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    public User getUser() {
        return user;
    }

    public String getTenantPath() {
        return user.getOrganization().getTenantPath();
    }

    public boolean isScopeEnabled() {
        return scopeEnabled;
    }

    private static Collection<GrantedAuthority> toAuthorities(Collection<String> permissions) {
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(toList());
    }
}
