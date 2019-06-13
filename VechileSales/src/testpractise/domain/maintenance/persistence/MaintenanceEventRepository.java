package com.dizzion.portal.domain.maintenance.persistence;

import com.dizzion.portal.domain.common.persistence.CrudAndSpecificationExecutorRepository;
import com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventEntity;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface MaintenanceEventRepository extends CrudAndSpecificationExecutorRepository<MaintenanceEventEntity> {
    @Query(value = "SELECT DISTINCT event.* " +
            "FROM maintenance_event event " +
            "  JOIN maintenance_event_organization mo ON event.id = mo.maintenance_event_id " +
            "  JOIN organization org ON mo.organization_id = org.id " +
            "WHERE DATE(start_date_time) <= :until " +
            "      AND (DATE(end_date_time) >= :from " +
            "           OR ((repeat_until IS NULL OR repeat_until >= :from) AND cron IS NOT NULL)) " +
            "      AND tenant_path LIKE :tenantPath " +
            "ORDER BY start_date_time ASC",
            nativeQuery = true)
    List<MaintenanceEventEntity> findInDateRange(LocalDate from, LocalDate until, String tenantPath);
}
