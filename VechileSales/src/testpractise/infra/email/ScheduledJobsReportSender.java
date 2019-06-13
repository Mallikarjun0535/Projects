package com.dizzion.portal.infra.email;

import com.dizzion.portal.domain.maintenance.MaintenanceEventEmailService;
import com.dizzion.portal.domain.maintenance.MaintenanceEventService;
import com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static java.time.ZonedDateTime.now;
import static java.util.stream.Collectors.toList;

@Component
@AllArgsConstructor
public class ScheduledJobsReportSender {

    private final AuthenticatedUserAccessor auth;
    private final MaintenanceEventService maintenanceEventService;
    private final MaintenanceEventEmailService maintenanceEventEmailService;

    @Scheduled(cron = "${scheduled-jobs.report.send.cron}")
    public void sendReport() {
        ZonedDateTime now = now();
        LocalDate localDateNow = LocalDate.now();

        auth.asAdmin(() -> {
            List<MaintenanceEvent> scheduledJobs = maintenanceEventService.getAllScheduledJobsInstancesBetween(localDateNow.minusDays(1), localDateNow.plusDays(1));
            List<MaintenanceEvent> yesterdayEvents = scheduledJobs
                    .stream()
                    .filter(job -> job.getStartDateTime().isBefore(now) && job.getStartDateTime().isAfter(now.minusDays(1)))
                    .collect(toList());

            List<MaintenanceEvent> todayEvents = scheduledJobs
                    .stream()
                    .filter(job -> job.getStartDateTime().isAfter(now) && job.getStartDateTime().isBefore(now.plusDays(1)))
                    .collect(toList());
            maintenanceEventEmailService.sendReport(yesterdayEvents, todayEvents);
        });

    }
}
