package com.dizzion.portal.domain.helpdesk.persistence;

import com.dizzion.portal.domain.helpdesk.persistence.entity.OrganizationTemperatureChangeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface OrganizationTemperatureChangeRepository extends PagingAndSortingRepository<OrganizationTemperatureChangeEntity, Long> {
    Page<OrganizationTemperatureChangeEntity> findByOrganizationId(Long id, Pageable pageRequest);

    @Query(value = "SELECT changes.* " +
            "FROM organization_temperature_change changes, " +
            "  (SELECT organization_id, MAX(timestamp) timestamp " +
            "   FROM organization_temperature_change " +
            "   GROUP BY organization_id) lastChanges " +
            "WHERE changes.organization_id = lastChanges.organization_id " +
            "      AND changes.timestamp = lastChanges.timestamp " +
            "      AND changes.organization_id IN (:organizationIds)",
            nativeQuery = true)
    List<OrganizationTemperatureChangeEntity> findLastChangeInEachOrganization(List<Long> organizationIds);
}
