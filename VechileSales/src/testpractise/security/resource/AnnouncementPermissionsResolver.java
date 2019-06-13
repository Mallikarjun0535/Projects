package com.dizzion.portal.security.resource;

import com.dizzion.portal.domain.announcement.dto.Announcement;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import org.springframework.stereotype.Component;

import static com.dizzion.portal.domain.scope.TenantPathUtils.isChildTenantPath;

@Component
public class AnnouncementPermissionsResolver extends ResourcePermissionsResolver<Announcement> {
    public AnnouncementPermissionsResolver(AuthenticatedUserAccessor auth) {
        super(auth);
    }

    @Override
    public boolean isEditable(Announcement announcement) {
        return isChildTenantPath(announcement.getOwnerTenantPath(), auth.getTenantPath());
    }
}
