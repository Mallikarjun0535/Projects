package com.dizzion.portal.domain.maintenance.persistence;

import com.dizzion.portal.domain.common.persistence.CrudAndSpecificationExecutorRepository;
import com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventProgressTicketEntity;

import java.util.Collection;

public interface MaintenanceEventProgressTicketRepository extends CrudAndSpecificationExecutorRepository<MaintenanceEventProgressTicketEntity> {
    Collection<MaintenanceEventProgressTicketEntity> findByMaintenanceEventInstanceId(long instanceId);
}
