package com.dizzion.portal.domain.common;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;
import java.util.function.Supplier;

@UtilityClass
@Slf4j
public class ExceptionUtils {
    public static void tolerateHttpExceptions(Runnable runnable) {
        try {
            runnable.run();
        } catch (HttpClientErrorException ex) {
            log.debug("Tolerating exception", ex);
        }
    }

    public static <T> Optional<T> optionalOnException(Supplier<T> func) {
        try {
            return Optional.ofNullable(func.get());
        } catch (Exception ex) {
            log.debug("Returning empty optional because of exception", ex);
            return Optional.empty();
        }
    }
}
