package com.dizzion.portal.domain.application.persistence;

import com.dizzion.portal.domain.application.persistence.entity.ApplicationGroupEntity;
import com.dizzion.portal.domain.common.persistence.CrudAndSpecificationExecutorRepository;

public interface ApplicationGroupRepository extends CrudAndSpecificationExecutorRepository<ApplicationGroupEntity> {
    ApplicationGroupEntity findByName(String name);
}