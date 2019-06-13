package com.dizzion.portal.security.resource;

import com.dizzion.portal.domain.role.RoleService;
import com.dizzion.portal.domain.role.dto.Role;
import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import org.springframework.stereotype.Component;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
public class UserPermissionsResolver extends ResourcePermissionsResolver<User> {
    private final RoleService roleService;

    public UserPermissionsResolver(AuthenticatedUserAccessor auth, RoleService roleService) {
        super(auth);
        this.roleService = roleService;
    }

    @Override
    public boolean isEditable(User user) {
        Set<String> subordinateRoles = roleService
                .getSubordinateRoles(auth.getAuthenticatedUser().getUser().getRole().getName()).stream()
                .map(Role::getName)
                .collect(toSet());
        return !user.getId().equals(auth.getAuthenticatedUser().getUser().getId()) &&
                subordinateRoles.contains(user.getRole().getName());
    }
}
