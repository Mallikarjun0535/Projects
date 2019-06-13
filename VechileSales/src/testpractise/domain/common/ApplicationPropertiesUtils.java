package com.dizzion.portal.domain.common;

import lombok.experimental.UtilityClass;

import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

@UtilityClass
public class ApplicationPropertiesUtils {

    public static Set<String> splitEmails(String emails) {
        return stream(emails.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .collect(toSet());
    }
}
