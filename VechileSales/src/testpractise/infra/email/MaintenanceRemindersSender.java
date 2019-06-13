package com.dizzion.portal.infra.email;

import com.dizzion.portal.domain.maintenance.MaintenanceEventEmailService;
import com.dizzion.portal.domain.maintenance.MaintenanceEventService;
import com.dizzion.portal.domain.maintenance.dto.CronTrigger;
import com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent;
import com.dizzion.portal.domain.maintenance.persistence.MaintenanceEventRepository;
import com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventEntity;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;

import static com.dizzion.portal.domain.scope.TenantPathUtils.tenantScope;

@Component
@Transactional
@AllArgsConstructor
public class MaintenanceRemindersSender {

    private final MaintenanceEventRepository maintenanceEventRepository;
    private final MaintenanceEventService maintenanceEventService;
    private final MaintenanceEventEmailService maintenanceEventEmailService;
    private final AuthenticatedUserAccessor auth;

    @Scheduled(cron = "${maintenance.reminder.check.cron}")
    public void sendReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        auth.asAdmin(() ->{
                    maintenanceEventRepository.findInDateRange(tomorrow, tomorrow, tenantScope(auth.getTenantPath()))
                            .parallelStream()
                            .filter(item -> item.getCron().isPresent())
                            .filter(this::isUnsentReminder)
                            .filter(eventEntity -> CronTrigger.from(eventEntity).triggersOn(tomorrow, eventEntity.getStartDateTime().toLocalTime()))
                            .forEach(eventEntity -> auth.asAdmin(() -> {
                                MaintenanceEvent eventInstance = maintenanceEventService.getMaintenanceEventInstanceForCron(eventEntity, tomorrow);
                                if(Objects.nonNull(eventInstance)){
                                    maintenanceEventEmailService.sendNotifications(eventInstance);
                                    eventEntity.setReminderSentAt(ZonedDateTime.now());
                                }
                            }));
                });
    }

    private boolean isUnsentReminder(MaintenanceEventEntity eventEntity) {
        return eventEntity.isReminder()
                && (eventEntity.getReminderSentAt() == null || eventEntity.getReminderSentAt().toLocalDate().isBefore(LocalDate.now()));
    }
}
