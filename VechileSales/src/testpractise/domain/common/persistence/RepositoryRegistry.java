package com.dizzion.portal.domain.common.persistence;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.springframework.core.GenericTypeResolver.resolveTypeArgument;

@Component
public class RepositoryRegistry {
    private final Map<Class<?>, CrudAndSpecificationExecutorRepository<?>> repos = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T> CrudAndSpecificationExecutorRepository<T> repositoryFor(Class<T> entityType) {
        System.out.println("+++++555555555555555555");
        return Optional.ofNullable((CrudAndSpecificationExecutorRepository<T>) repos.get(entityType))
                .orElseThrow(() -> new IllegalArgumentException("Repository not found for entity=" + entityType.getCanonicalName()));
    }

    @EventListener
    protected void onApplicationEvent(ContextRefreshedEvent event) {
        event.getApplicationContext()
                .getBeansOfType(CrudAndSpecificationExecutorRepository.class)
                .values()
                .forEach(repo -> repos.put(getTargetEntityClass(repo), repo));
    }

    private Class<?> getTargetEntityClass(CrudAndSpecificationExecutorRepository repo) {
        return resolveTypeArgument(repo.getClass(), CrudAndSpecificationExecutorRepository.class);
    }
}
