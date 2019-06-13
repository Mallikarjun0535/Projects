package com.dizzion.portal.security.resource;

import com.dizzion.portal.domain.notification.dto.Notification;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import org.springframework.stereotype.Component;

import static com.dizzion.portal.domain.scope.TenantPathUtils.isChildTenantPath;

@Component
public class NotificationPermissionsResolver extends ResourcePermissionsResolver<Notification> {
    public NotificationPermissionsResolver(AuthenticatedUserAccessor auth) {
        super(auth);
    }

    @Override
    public boolean isEditable(Notification notification) {
        return isChildTenantPath(notification.getOwnerTenantPath(), auth.getTenantPath());
    }
}
