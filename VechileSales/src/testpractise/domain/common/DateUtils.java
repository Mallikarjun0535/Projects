package com.dizzion.portal.domain.common;

import lombok.experimental.UtilityClass;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Stream.iterate;

@UtilityClass
public class DateUtils {

    private static final SimpleDateFormat RFC_2822_FORMATTER = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");

    public static Stream<LocalDate> streamDates(LocalDate start, LocalDate end) {
        long rangeDuration = DAYS.between(start, end) + 1;
        return iterate(start, date -> date.plusDays(1))
                .limit(rangeDuration > 0 ? rangeDuration : 0);
    }

    public static String getCurrentDateInRfc2822() {
        return RFC_2822_FORMATTER.format(new Date());
    }
}
