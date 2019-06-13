package com.dizzion.portal.domain.organization.persistence;

import com.dizzion.portal.domain.common.persistence.CrudAndSpecificationExecutorRepository;
import com.dizzion.portal.domain.organization.dto.Organization.OrganizationType;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends CrudAndSpecificationExecutorRepository<OrganizationEntity> {

    OrganizationEntity findByName(String name);

    Optional<OrganizationEntity> findByCustomerId(String customerId);

    Optional<OrganizationEntity> findByCustomerIdAndTenantPathLike(String customerId, String tenantPath);

    List<OrganizationEntity> findByTypeAndTenantPathLike(OrganizationType type, String tenantPath);

    List<OrganizationEntity> findByNameInAndTenantPathLike(List<String> names, String tenantPath);
}
