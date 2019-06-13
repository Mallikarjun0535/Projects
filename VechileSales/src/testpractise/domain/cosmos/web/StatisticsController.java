package com.dizzion.portal.domain.cosmos.web;

import com.dizzion.portal.domain.cosmos.StatisticsService;
import com.dizzion.portal.domain.cosmos.dto.PoolStatistics;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static com.dizzion.portal.domain.role.Permission.Constants.VIEW_STATISTICS;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class StatisticsController {
    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @RequestMapping(path = "/utilization-statistics", method = GET)
    @Secured(VIEW_STATISTICS)
    public List<PoolStatistics> getUtilizationStatistics(@RequestParam("orgId") Long orgId,
                                                         @RequestParam("dateFrom")
                                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                 LocalDate dateFrom) {
        return statisticsService.getUtilizationStatistics(orgId, dateFrom);
    }
}
