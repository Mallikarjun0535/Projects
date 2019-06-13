package com.dizzion.portal.domain.organization.persistence;

import com.dizzion.portal.domain.common.persistence.CrudAndSpecificationExecutorRepository;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationGroupEntity;

public interface OrganizationGroupRepository extends CrudAndSpecificationExecutorRepository<OrganizationGroupEntity> {
    OrganizationGroupEntity findByName(String name);
}