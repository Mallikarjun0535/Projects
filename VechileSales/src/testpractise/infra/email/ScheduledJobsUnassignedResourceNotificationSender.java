package com.dizzion.portal.infra.email;

import com.dizzion.portal.domain.maintenance.MaintenanceEventEmailService;
import com.dizzion.portal.domain.maintenance.MaintenanceEventService;
import com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@AllArgsConstructor
public class ScheduledJobsUnassignedResourceNotificationSender {

    private final AuthenticatedUserAccessor auth;
    private final MaintenanceEventService maintenanceEventService;
    private final MaintenanceEventEmailService maintenanceEventEmailService;

    @Scheduled(cron = "${scheduled-jobs.unassigned-resource.send.cron}")
    public void sendReport() {
        LocalDate localDateNow = LocalDate.now();

        auth.asAdmin(() -> {
            List<MaintenanceEvent> scheduledJobs = maintenanceEventService.getAllScheduledJobsInstancesBetween(localDateNow, localDateNow.plusDays(1));
            scheduledJobs.stream()
                    .filter(job -> !job.getAssignedUser().isPresent())
                    .forEach(maintenanceEventEmailService::sendUnassignedResourceNotification);
        });

    }
}
