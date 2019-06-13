package com.dizzion.portal.security.resource;

import com.dizzion.portal.domain.organization.dto.Organization;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import org.springframework.stereotype.Component;

import static com.dizzion.portal.domain.scope.TenantPathUtils.isChildTenantPath;

@Component
public class OrganizationPermissionsResolver extends ResourcePermissionsResolver<Organization> {
    public OrganizationPermissionsResolver(AuthenticatedUserAccessor auth) {
        super(auth);
    }

    @Override
    public boolean isEditable(Organization org) {
        return isChildTenantPath(org.getTenantPath(), auth.getTenantPath()) &&
                !auth.getOrganization().getId().equals(org.getId());
    }
}
