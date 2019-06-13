package com.dizzion.portal.security.resource;

import com.dizzion.portal.domain.application.dto.Application;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import org.springframework.stereotype.Component;

import static com.dizzion.portal.domain.scope.TenantPathUtils.isChildTenantPath;

@Component
public class ApplicationPermissionsResolver extends ResourcePermissionsResolver<Application> {
    public ApplicationPermissionsResolver(AuthenticatedUserAccessor auth) {
        super(auth);
    }

    @Override
    public boolean isEditable(Application app) {
        return isChildTenantPath(app.getOwnerTenantPath(), auth.getTenantPath());
    }
}
