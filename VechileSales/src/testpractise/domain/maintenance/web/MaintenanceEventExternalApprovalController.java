package com.dizzion.portal.domain.maintenance.web;

import com.dizzion.portal.domain.common.DateFormatService;
import com.dizzion.portal.domain.maintenance.MaintenanceEventService;
import com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent;
import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Controller
public class MaintenanceEventExternalApprovalController {

    private final MaintenanceEventService maintenanceEventService;
    private final DateFormatService dateFormatService;

    public MaintenanceEventExternalApprovalController(MaintenanceEventService maintenanceEventService,
                                                      DateFormatService dateFormatService) {
        this.maintenanceEventService = maintenanceEventService;
        this.dateFormatService = dateFormatService;
    }

    @GetMapping("/maintenance-events/{id}/external-approval")
    public ModelAndView externalApproval(@PathVariable long id, @RequestParam boolean approve) {
        try {
            if (approve) {
                MaintenanceEvent maintenanceEvent = maintenanceEventService.approve(id);
                return new ModelAndView("maintenance-approval-response", ImmutableMap.of(
                        "maintenanceEvent", maintenanceEvent,
                        "startDateTime", dateFormatService.formatDateTime(maintenanceEvent.getStartDateTime()),
                        "endDateTime", dateFormatService.formatDateTime(maintenanceEvent.getEndDateTime()),
                        "result", "Approved"));
            } else {
                MaintenanceEvent maintenanceEvent = maintenanceEventService.reject(id);
                return new ModelAndView("maintenance-approval-response", ImmutableMap.of(
                        "maintenanceEvent", maintenanceEvent,
                        "startDateTime", dateFormatService.formatDateTime(maintenanceEvent.getStartDateTime()),
                        "endDateTime", dateFormatService.formatDateTime(maintenanceEvent.getEndDateTime()),
                        "result", "Rejected"));
            }
        } catch (Exception e) {
            String errorReason = isBlank(e.getLocalizedMessage())
                    ? "Error during approval process"
                    : e.getLocalizedMessage();
            return new ModelAndView("maintenance-approval-response", ImmutableMap.of("result", errorReason));
        }
    }
}
