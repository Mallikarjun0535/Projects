package com.dizzion.portal.domain.scope.persistence;

import com.dizzion.portal.domain.scope.TenantPathUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.dizzion.portal.domain.scope.TenantPathUtils.tenantScope;

public class ScopeSpecification<T> implements Specification<T> {
    private final TenantPathFieldResolver<T> tenantPathFieldResolver;
    private final String tenantPath;
    private final Optional<Collection<Long>> entitiesIds;
    private final Optional<Long> entityId;

    public ScopeSpecification(TenantPathFieldResolver<T> tenantPathFieldResolver, String tenantPath) {
        this.tenantPath = tenantPath;
        this.tenantPathFieldResolver = tenantPathFieldResolver;
        this.entitiesIds = Optional.empty();
        this.entityId = Optional.empty();
    }

    public ScopeSpecification(long entityId, TenantPathFieldResolver<T> tenantPathFieldResolver, String tenantPath) {
        this.tenantPath = tenantPath;
        this.tenantPathFieldResolver = tenantPathFieldResolver;
        this.entitiesIds = Optional.empty();
        this.entityId = Optional.of(entityId);
    }

    public ScopeSpecification(Collection<Long> entitiesIds, TenantPathFieldResolver<T> tenantPathFieldResolver, String tenantPath) {
        this.tenantPath = tenantPath;
        this.tenantPathFieldResolver = tenantPathFieldResolver;
        this.entitiesIds = Optional.of(entitiesIds);
        this.entityId = Optional.empty();
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        query.distinct(true);
        List<Predicate> predicates = new ArrayList<>();
        entityId.ifPresent(entityId -> predicates.add(cb.equal(root.get("id"), entityId)));
        entitiesIds.ifPresent(entitiesIds -> predicates.add(root.get("id").in(entitiesIds)));
        if (!TenantPathUtils.isPortalAdmin(tenantPath)) {
            predicates.add(cb.like(tenantPathFieldResolver.resolveFrom(root), tenantScope(tenantPath)));
        }
        return cb.and(predicates.toArray(new Predicate[predicates.size()]));
    }
}
