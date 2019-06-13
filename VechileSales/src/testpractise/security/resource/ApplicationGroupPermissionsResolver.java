package com.dizzion.portal.security.resource;

import com.dizzion.portal.domain.application.dto.ApplicationGroup;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import org.springframework.stereotype.Component;

import static com.dizzion.portal.domain.scope.TenantPathUtils.isChildTenantPath;

@Component
public class ApplicationGroupPermissionsResolver extends ResourcePermissionsResolver<ApplicationGroup> {
    public ApplicationGroupPermissionsResolver(AuthenticatedUserAccessor auth) {
        super(auth);
    }

    @Override
    public boolean isEditable(ApplicationGroup appGroup) {
        return isChildTenantPath(appGroup.getOwnerTenantPath(), auth.getTenantPath());
    }
}
