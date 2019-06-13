package com.dizzion.portal.security.resource;

import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;

public abstract class ResourcePermissionsResolver<T> {

    protected final AuthenticatedUserAccessor auth;

    public ResourcePermissionsResolver(AuthenticatedUserAccessor auth) {
        this.auth = auth;
    }

    public ResourceWithPermissions<T> enrichWithPermissions(T resource) {
        return new ResourceWithPermissions<>(resource, isEditable(resource));
    }

    public abstract boolean isEditable(T resource);
}
