package com.dizzion.portal.domain.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@Service
public class DateFormatService {
    private final ZoneId messagesTimeZoneId;

    public DateFormatService(@Value("${emergency.message.time-zone}") String messagesTimeZone) {
        this.messagesTimeZoneId = TimeZone.getTimeZone(messagesTimeZone).toZoneId();
    }

    public String formatDateTime(ZonedDateTime dateTime) {
        return dateTime.withZoneSameInstant(messagesTimeZoneId)
                .format(DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a (z)"));
    }

    public String formatTime(ZonedDateTime dateTime) {
        return dateTime.withZoneSameInstant(messagesTimeZoneId)
                .format(DateTimeFormatter.ofPattern("h:mm a"));
    }
}
