package com.dizzion.portal.domain.scope;

import lombok.experimental.UtilityClass;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

@UtilityClass
public class TenantPathUtils {

    private static final String PORTAL_ADMIN_TENANT_PATH = "1/";

    public static String tenantPath(String parentTenantPath, long childId) {
        return parentTenantPath + childId + "/";
    }

    public static String tenantPath(long... ids) {
        return stream(ids).mapToObj(String::valueOf).collect(joining("/")) + "/";
    }

    public static String tenantScope(String tenantPath) {
        return tenantPath + "%";
    }

    public static boolean isPortalAdmin(String tenantPath) {
        return PORTAL_ADMIN_TENANT_PATH.equals(tenantPath);
    }

    public static boolean isChildTenantPath(String childTenantPath, String parentTenantPath) {
        return childTenantPath.startsWith(parentTenantPath);
    }
}
