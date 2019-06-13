package com.dizzion.portal.security.resource;

import com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import org.springframework.stereotype.Component;

import static com.dizzion.portal.domain.scope.TenantPathUtils.isChildTenantPath;

@Component
public class MaintenanceEventPermissionsResolver extends ResourcePermissionsResolver<MaintenanceEvent> {
    public MaintenanceEventPermissionsResolver(AuthenticatedUserAccessor auth) {
        super(auth);
    }

    @Override
    public boolean isEditable(MaintenanceEvent maintenanceEvent) {
        return isChildTenantPath(maintenanceEvent.getOwnerTenantPath(), auth.getTenantPath());
    }
}
