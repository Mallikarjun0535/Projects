package com.dizzion.portal.domain.common;

import com.dizzion.portal.domain.common.persistence.RepositoryRegistry;
import com.dizzion.portal.domain.exception.EntityNotFoundException;
import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.filter.persistence.FilterSpecificationFactory;
import com.dizzion.portal.domain.scope.persistence.ScopeSpecificationFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;

@Service
@Transactional
public class ScopedEntityService {
    private final RepositoryRegistry repoRegistry;
    private final ScopeSpecificationFactory scopeSpecFactory;
    private final FilterSpecificationFactory filterSpecFactory;
    private final JpaContext jpaContext;

    private final int batchSize;

    public ScopedEntityService(ScopeSpecificationFactory scopeSpecFactory,
                               FilterSpecificationFactory filterSpecFactory,
                               RepositoryRegistry repoRegistry,
                               JpaContext jpaContext,
                               @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}") int batchSize) {
        this.scopeSpecFactory = scopeSpecFactory;
        this.repoRegistry = repoRegistry;
        this.filterSpecFactory = filterSpecFactory;
        this.jpaContext = jpaContext;
        this.batchSize = batchSize;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public <T> T getForRead(long entityId, Class<T> entityClass) {
        Specification<T> spec = scopeSpecFactory.readOnlySpecificationFor(entityId, entityClass);
        return Optional.ofNullable(repoRegistry.repositoryFor(entityClass).findOne(spec))
                .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public <T> Set<T> getForRead(Collection<Long> entityIds, Class<T> entityClass) {
        if (entityIds.isEmpty()) {
            return emptySet();
        }
        Specification<T> spec = scopeSpecFactory.readOnlySpecificationFor(entityIds, entityClass);
        Set<T> entities = new HashSet<>(repoRegistry.repositoryFor(entityClass).findAll(spec));

        if (entities.size() != entityIds.size()) {
            throw new EntityNotFoundException();
        }
        return entities;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public <T> T getForWrite(long entityId, Class<T> entityClass) {
        Specification<T> spec = scopeSpecFactory.editableSpecificationFor(entityId, entityClass);
        return Optional.ofNullable(repoRegistry.repositoryFor(entityClass).findOne(spec))
                .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public <T> Set<T> getForWrite(Collection<Long> entityIds, Class<T> entityClass) {
        if (entityIds.isEmpty()) {
            return emptySet();
        }
        Specification<T> spec = scopeSpecFactory.editableSpecificationFor(entityIds, entityClass);
        Set<T> entities = new HashSet<>(repoRegistry.repositoryFor(entityClass).findAll(spec));

        if (entities.size() != entityIds.size()) {
            throw new EntityNotFoundException();
        }
        return entities;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public <T> Page<T> getPage(Class<T> entityClass, Pageable pageRequest, Set<FieldFilter> filters) {
        Specification<T> spec = scopeSpecFactory.readOnlySpecificationFor(entityClass)
                .and(filterSpecFactory.specificationFor(entityClass, filters));
        System.out.println("+++++44444444444444444444444444");
        return repoRegistry.repositoryFor(entityClass).findAll(spec, pageRequest);
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public <T> Page<T> getPage(Class<T> entityClass, Pageable pageRequest, Set<FieldFilter> filters, String tenantPath) {
        Specification<T> spec = scopeSpecFactory.readOnlySpecificationFor(entityClass, tenantPath)
                .and(filterSpecFactory.specificationFor(entityClass, filters));
        return repoRegistry.repositoryFor(entityClass).findAll(spec, pageRequest);
    }

    public <T> void checkAccessAndDelete(long id, Class<T> entityClass) {
        throwExceptionIfNotAccessible(id, entityClass);
        repoRegistry.repositoryFor(entityClass).delete(id);
    }

    @SuppressWarnings("unchecked")
    public <T> T save(T entity) {
        return repoRegistry.repositoryFor((Class<T>) entity.getClass()).save(entity);
    }

    @Transactional(readOnly = true)
    public void throwExceptionIfNotAccessible(long entityId, Class<?> entityClass) {
        this.getForWrite(entityId, entityClass);
    }

    @Transactional(readOnly = true)
    public void throwExceptionIfNotAccessible(Collection<Long> entitiesIds, Class<?> entityClass) {
        this.getForWrite(entitiesIds, entityClass);
    }

    public <T> Set<T> save(Collection<T> entities, Class<T> entityClass) {
        EntityManager entityManager = jpaContext.getEntityManagerByManagedType(entityClass);
        Set<T> persistedEntities = new HashSet<>();

        int i = 0;
        for (T entity : entities) {
            entityManager.persist(entity);
            persistedEntities.add(entity);
            i++;
            if (i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        return persistedEntities;
    }
}
