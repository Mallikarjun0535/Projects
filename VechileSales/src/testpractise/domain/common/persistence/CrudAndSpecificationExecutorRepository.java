package com.dizzion.portal.domain.common.persistence;


import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CrudAndSpecificationExecutorRepository<T> extends CrudRepository<T, Long>,
        JpaSpecificationExecutor<T> {
}
