package com.dizzion.portal.domain.role.web;

import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.organization.dto.Organization;
import com.dizzion.portal.domain.role.Permission;
import com.dizzion.portal.domain.role.RoleService;
import com.dizzion.portal.domain.role.dto.Role;
import com.dizzion.portal.domain.role.dto.RoleCreateUpdateRequest;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import com.dizzion.portal.security.resource.ResourceWithPermissions;
import com.dizzion.portal.security.resource.RolePermissionResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;

import static com.dizzion.portal.domain.role.Permission.Constants.ROLE_MANAGEMENT;

@RestController
public class RoleController {

    private final RoleService roleService;
    private final RolePermissionResolver permissionsResolver;
    private final AuthenticatedUserAccessor auth;

    public RoleController(RoleService roleService, RolePermissionResolver permissionsResolver, AuthenticatedUserAccessor auth) {
        this.roleService = roleService;
        this.permissionsResolver = permissionsResolver;
        this.auth = auth;
    }

    @GetMapping("/roles")
    @Secured(ROLE_MANAGEMENT)
    public Page<ResourceWithPermissions<Role>> getRoles(Pageable pageRequest, Set<FieldFilter> filters) {
        return roleService.getRoles(pageRequest, filters).map(permissionsResolver::enrichWithPermissions);
    }

    @GetMapping("/subordinate-roles")
    public Set<Role> getSubordinateRoles() {
        return roleService.getSubordinateRoles(getAuthRoleName());
    }

    @GetMapping("/organization-type/{type}/roles")
    public Set<Role> getRolesAvailableForOrganizationType(@PathVariable Organization.OrganizationType type) {
        return roleService.getRolesAvailableForOrganizationType(getAuthRoleName(), type);
    }

    @GetMapping("/permissions")
    @Secured(ROLE_MANAGEMENT)
    public Set<Permission> getPermissions() {
        return roleService.getPermissions();
    }

    @GetMapping("/roles/name/uniqueness")
    @Secured(ROLE_MANAGEMENT)
    public boolean checkNameUnique(String roleName) {
        return roleService.isNameAvailable(roleName);
    }

    @PostMapping("/roles")
    @Secured(ROLE_MANAGEMENT)
    public Role create(@RequestBody @Valid RoleCreateUpdateRequest role) {
        return roleService.create(role);
    }

    @PutMapping("/roles/{id}")
    @Secured(ROLE_MANAGEMENT)
    public Role update(@PathVariable long id, @RequestBody @Valid RoleCreateUpdateRequest role) {
        return roleService.update(id, role);
    }

    @DeleteMapping("/roles/{id}")
    @Secured(ROLE_MANAGEMENT)
    public void delete(@PathVariable long id) {
        roleService.delete(id);
    }

    private String getAuthRoleName() {
        return auth.getAuthenticatedUser().getUser().getRole().getName();
    }
}
