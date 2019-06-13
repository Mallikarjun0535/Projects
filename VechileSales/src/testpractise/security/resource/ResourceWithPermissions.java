package com.dizzion.portal.security.resource;

import lombok.Value;

@Value
public class ResourceWithPermissions<T> {
    T payload;
    boolean editable;
}
