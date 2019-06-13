package com.dizzion.portal.domain.scope.persistence;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TenantResource {
    String readerTenantPath() default "tenantPath";

    String writerTenantPath() default "owner.tenantPath";
}
