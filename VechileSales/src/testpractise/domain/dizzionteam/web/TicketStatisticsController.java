package com.dizzion.portal.domain.dizzionteam.web;

import com.dizzion.portal.domain.dizzionteam.DizzionTeamService;
import com.dizzion.portal.domain.dizzionteam.dto.OrganizationTicketStatistic;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class TicketStatisticsController {

    private final DizzionTeamService dizzionTeamService;

    public TicketStatisticsController(DizzionTeamService dizzionTeamService) {
        this.dizzionTeamService = dizzionTeamService;
    }

    @RequestMapping(path = "/ticket-statistic", method = GET)
    public Set<OrganizationTicketStatistic> getTicketStatistics() {
        return dizzionTeamService.getTicketStatistics();
    }

}
