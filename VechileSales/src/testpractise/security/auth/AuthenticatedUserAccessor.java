package com.dizzion.portal.security.auth;

import com.dizzion.portal.domain.organization.dto.Organization;
import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.security.AdminProvider;
import com.dizzion.portal.security.auth.spring.AuthenticatedUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticatedUserAccessor {
    private final User admin;

    public AuthenticatedUserAccessor(AdminProvider adminProvider) {
        this.admin = adminProvider.getAdmin();
    }

    public AuthenticatedUser getAuthenticatedUser() {
        return (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication();
    }

    public void setAuthenticatedUser(User user) {
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedUser(user, false));
    }

    public String getTenantPath() {
        return getAuthenticatedUser().getTenantPath();
    }

    public String getCustomerId() {
        return getAuthenticatedUser().getUser().getOrganization().getCustomerId();
    }

    public boolean isPortalAdmin() {
        return getAuthenticatedUser().getUser().isPortalAdmin();
    }

    public Organization getOrganization() {
        return getAuthenticatedUser().getUser().getOrganization();
    }

    public void asAdmin(Runnable runnable) {
        Optional<User> currentUser = Optional.ofNullable(getAuthenticatedUser()).map(AuthenticatedUser::getUser);
        setAuthenticatedUser(admin);
        runnable.run();
        currentUser.ifPresent(this::setAuthenticatedUser);
    }
}
