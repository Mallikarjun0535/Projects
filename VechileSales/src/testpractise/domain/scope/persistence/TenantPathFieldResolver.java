package com.dizzion.portal.domain.scope.persistence;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

@FunctionalInterface
public interface TenantPathFieldResolver<T> {
    Path<String> resolveFrom(Root<T> criteriaRoot);
}
