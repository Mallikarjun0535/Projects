package com.dizzion.portal.domain.common;

import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.filter.persistence.FilterSpecification;
import com.dizzion.portal.domain.user.persistence.UserRepository;
import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import com.dizzion.portal.security.resource.UserPermissionsResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.springframework.core.GenericTypeResolver.resolveTypeArgument;

@Transactional
public abstract class AbstractCrudService<T> {
    private final Class<T> entityClass;
    protected final ScopedEntityService scopedEntityService;

    public AbstractCrudService(ScopedEntityService scopedEntityService) {
        this.scopedEntityService = scopedEntityService;
        this.entityClass = getEntityClass();
    }

    @SuppressWarnings("unchecked")
    protected Class<T> getEntityClass() {
        return (Class<T>) resolveTypeArgument(this.getClass(), AbstractCrudService.class);
    }

    @Transactional(readOnly = true)
    public Page<T> getEntitiesPage(Pageable pageRequest, Set<FieldFilter> filters) {
        System.out.println("+++++3333333333333333333333");
        return scopedEntityService.getPage(entityClass, pageRequest, filters);
    }

    @Transactional(readOnly = true)
    public T getForWrite(long id) {
        return scopedEntityService.getForWrite(id, entityClass);
    }

    @Transactional(readOnly = true)
    public T getForRead(long id) {
        return scopedEntityService.getForRead(id, entityClass);
    }

    @Transactional(readOnly = true)
    public void checkAccess(long id) {
        scopedEntityService.throwExceptionIfNotAccessible(id, entityClass);
    }

    public void delete(long id) {
        scopedEntityService.checkAccessAndDelete(id, entityClass);
    }

    public T save(T entity) {
        return scopedEntityService.save(entity);
    }
}
