package com.dizzion.portal.domain.maintenance.persistence;

import com.dizzion.portal.domain.common.persistence.CrudAndSpecificationExecutorRepository;
import com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventApprovalEntity;

import java.util.Optional;

public interface MaintenanceEventApprovalRepository extends CrudAndSpecificationExecutorRepository<MaintenanceEventApprovalEntity> {
    Optional<MaintenanceEventApprovalEntity> findByMaintenanceEventIdAndUserId(long eventId, long userId);
}
