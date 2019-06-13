package com.dizzion.portal.security.resource;

import com.dizzion.portal.domain.role.RoleService;
import com.dizzion.portal.domain.role.dto.Role;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import org.springframework.stereotype.Component;

@Component
public class RolePermissionResolver extends ResourcePermissionsResolver<Role> {
    private final RoleService roleService;

    public RolePermissionResolver(AuthenticatedUserAccessor auth, RoleService roleService) {
        super(auth);
        this.roleService = roleService;
    }

    @Override
    public boolean isEditable(Role role) {
        Role authRole = auth.getAuthenticatedUser().getUser().getRole();
        return !role.getId().equals(authRole.getId()) && roleService.getSubordinateRoles(authRole.getName()).contains(role);
    }
}
