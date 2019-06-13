package com.dizzion.portal.domain.maintenance.persistence;

import com.dizzion.portal.domain.common.persistence.CrudAndSpecificationExecutorRepository;
import com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent.ProgressStatus;
import com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventInstanceEntity;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface MaintenanceEventInstanceRepository extends CrudAndSpecificationExecutorRepository<MaintenanceEventInstanceEntity> {
    Collection<MaintenanceEventInstanceEntity> findByMaintenanceEventIdAndStartDateTimeIn(long eventId, Collection<ZonedDateTime> startDateTimes);

    Optional<MaintenanceEventInstanceEntity> findByMaintenanceEventIdAndStartDateTime(long eventId, ZonedDateTime startDateTime);

    Set<MaintenanceEventInstanceEntity> findByProgressStatusAndAssignedUserIsNullAndStartDateTimeBefore(ProgressStatus progressStatus, ZonedDateTime dateBefore);
}
