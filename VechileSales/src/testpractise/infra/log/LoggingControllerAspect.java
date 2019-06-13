package com.dizzion.portal.infra.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingControllerAspect {

    @Before("within(@org.springframework.web.bind.annotation.RestController *)")
    public void logBefore(JoinPoint joinPoint) {
        log.info("[Method= " + joinPoint.getSignature().toShortString() + "], "
                + "[Arguments= " + Arrays.toString(joinPoint.getArgs()) + "]");
    }
}