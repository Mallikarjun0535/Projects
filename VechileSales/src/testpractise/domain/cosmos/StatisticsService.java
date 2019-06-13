package com.dizzion.portal.domain.cosmos;

import com.dizzion.portal.domain.cosmos.dto.DailyPoolStatistics;
import com.dizzion.portal.domain.cosmos.dto.PoolStatistics;
import com.dizzion.portal.domain.cosmos.persistence.CosmosDbRecord;
import com.dizzion.portal.domain.cosmos.persistence.UtilizationReportingDao;
import com.dizzion.portal.domain.organization.OrganizationService;
import com.dizzion.portal.domain.organization.dto.Organization;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

@Service
public class StatisticsService {

    private final UtilizationReportingDao utilizationReportingDao;
    private final OrganizationService organizationService;

    public StatisticsService(UtilizationReportingDao utilizationReportingDao,
                             OrganizationService organizationService) {
        this.utilizationReportingDao = utilizationReportingDao;
        this.organizationService = organizationService;
    }

    @Transactional(readOnly = true)
    public List<PoolStatistics> getUtilizationStatistics(Long orgId, LocalDate dateFrom) {
        Organization organization = organizationService.getOrganization(orgId);
        return utilizationReportingDao.getUtilizationStatistics(organization.getCustomerId(), dateFrom)
                .stream()
                .collect(groupingBy(CosmosDbRecord::getPoolName,
                        mapping(Function.identity(), toList())))
                .entrySet()
                .stream()
                .map(entry -> PoolStatistics.builder()
                        .poolName(entry.getKey())
                        .dailyPoolStatistics(entry.getValue().stream()
                                .map(cosmosDbRecord -> DailyPoolStatistics.builder()
                                        .provisionedDesktopsCount(cosmosDbRecord.getProvisionedDesktopsCount())
                                        .statisticsCapturingDate(cosmosDbRecord.getStatisticsCapturingDate())
                                        .build())
                                .collect(toList()))
                        .build())
                .collect(toList());
    }
}
