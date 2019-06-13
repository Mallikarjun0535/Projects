package com.dizzion.portal.domain.scope.persistence;

import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;

import java.util.Collection;

import static java.util.Collections.singletonList;

@Component
public class ScopeSpecificationFactory<T> {
    private final AuthenticatedUserAccessor auth;
    private final TenantPathFieldResolverRegistry tenantPathFieldResolverRegistry;

    public ScopeSpecificationFactory(AuthenticatedUserAccessor auth,
                                     TenantPathFieldResolverRegistry tenantPathFieldResolverRegistry) {
        this.auth = auth;
        this.tenantPathFieldResolverRegistry = tenantPathFieldResolverRegistry;
    }

    public Specifications<T> readOnlySpecificationFor(Class<T> entityClass) {
        return readOnlySpecificationFor(entityClass, auth.getTenantPath());
    }

    public Specifications<T> readOnlySpecificationFor(Class<T> entityClass, String tenantPath) {
        return Specifications.where(new ScopeSpecification<>(
                tenantPathFieldResolverRegistry.readerTenantPathFieldResolverFor(entityClass),
                tenantPath));
    }

    public Specifications<T> readOnlySpecificationFor(long id, Class<T> entityClass) {
        return readOnlySpecificationFor(singletonList(id), entityClass);
    }

    public Specifications<T> readOnlySpecificationFor(Collection<Long> ids, Class<T> entityClass) {
        return Specifications.where(new ScopeSpecification<>(ids,
                tenantPathFieldResolverRegistry.readerTenantPathFieldResolverFor(entityClass),
                auth.getTenantPath()));
    }

    public Specifications<T> editableSpecificationFor(long id, Class<T> entityClass) {
        return editableSpecificationFor(singletonList(id), entityClass);
    }

    public Specifications<T> editableSpecificationFor(Collection<Long> ids, Class<T> entityClass) {
        return Specifications.where(new ScopeSpecification<>(ids,
                tenantPathFieldResolverRegistry.writerTenantPathFieldResolverFor(entityClass),
                auth.getTenantPath()));
    }
}
