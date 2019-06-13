package com.dizzion.portal.domain.scope.persistence;

import com.dizzion.portal.domain.common.persistence.CriteriaPathFactory;
import org.reflections.Reflections;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class TenantPathFieldResolverRegistry {
    private final Map<Class<?>, TenantPathFieldResolver<?>> readerTenantPathFieldResolvers = new HashMap<>();
    private final Map<Class<?>, TenantPathFieldResolver<?>> writerTenantPathFieldResolvers = new HashMap<>();

    public TenantPathFieldResolverRegistry(CriteriaPathFactory pathFactory) {
        new Reflections("").getTypesAnnotatedWith(TenantResource.class)
                .forEach(tenantResourceClazz -> {
                    TenantResource annotation = tenantResourceClazz.getAnnotation(TenantResource.class);
                    readerTenantPathFieldResolvers.put(tenantResourceClazz, criteriaRoot ->
                            pathFactory.criteriaPathFromString(annotation.readerTenantPath(), criteriaRoot));
                    writerTenantPathFieldResolvers.put(tenantResourceClazz, criteriaRoot ->
                            pathFactory.criteriaPathFromString(annotation.writerTenantPath(), criteriaRoot));
                });
    }

    @SuppressWarnings("unchecked")
    public <T> TenantPathFieldResolver<T> readerTenantPathFieldResolverFor(Class<T> clazz) {
        return Optional.ofNullable((TenantPathFieldResolver<T>) readerTenantPathFieldResolvers.get(clazz))
                .orElseThrow(NullPointerException::new);
    }

    @SuppressWarnings("unchecked")
    public <T> TenantPathFieldResolver<T> writerTenantPathFieldResolverFor(Class<T> clazz) {
        return Optional.ofNullable((TenantPathFieldResolver<T>) writerTenantPathFieldResolvers.get(clazz))
                .orElseThrow(NullPointerException::new);
    }
}
