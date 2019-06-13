package com.dizzion.portal.domain.maintenance.dto;

import com.cronutils.model.definition.CronConstraintsFactory;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventEntity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.MINUTES;

public class CronTrigger {
    private final static CronParser cronParser = new CronParser(CronDefinitionBuilder.defineCron()
            .withSeconds().and()
            .withMinutes().and()
            .withHours().and()
            .withDayOfMonth().supportsL().supportsW().supportsLW().supportsQuestionMark().and()
            .withMonth().and()
            .withDayOfWeek().withValidRange(1, 7).withMondayDoWValue(1).supportsHash().supportsL().supportsQuestionMark().and()
            .withCronValidation(CronConstraintsFactory.ensureEitherDayOfWeekOrDayOfMonth())
            .instance());

    private ZonedDateTime startDateTime;
    private ExecutionTime executionTime;
    private ZoneId timezone;

    public CronTrigger(ZonedDateTime startDateTime, String timezoneOffset, String cron) {
        this.timezone = ZoneId.of(timezoneOffset);
        this.startDateTime = startDateTime.withZoneSameInstant(timezone);
        this.executionTime = ExecutionTime.forCron(cronParser.parse(cron));
    }

    public static CronTrigger from(MaintenanceEventEntity entity) {
        String cron = entity.getCron().orElseThrow(() -> new IllegalArgumentException("Non-recurring job, id=" + entity.getId()));
        return new CronTrigger(entity.getStartDateTime(), entity.getTimezoneOffset(), cron);
    }

    public boolean triggersOn(LocalDate date, LocalTime time) {
        ZonedDateTime triggersOnDate = date
                .atStartOfDay(ZoneId.systemDefault())
                .with(time.truncatedTo(MINUTES))
                .withZoneSameInstant(timezone);
        return !startDateTime.isAfter(triggersOnDate) && executionTime.isMatch(triggersOnDate);
    }

    public Optional<LocalDate> nextOccurrence(ZonedDateTime dateTime) {
        ZonedDateTime dateTimeInCronTimezone = dateTime
                .truncatedTo(MINUTES)
                .withZoneSameInstant(timezone);
        return executionTime.nextExecution(dateTimeInCronTimezone)
                .map(nextExecutionDateTime -> nextExecutionDateTime.isAfter(startDateTime)
                        ? nextExecutionDateTime
                        : startDateTime)
                .map(nextExecutionDateTime -> nextExecutionDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate());
    }
}
