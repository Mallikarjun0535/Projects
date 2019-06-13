package com.dizzion.portal.infra.log;

import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.security.auth.spring.AuthenticatedUser;
import org.slf4j.MDC;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ThreadLocalRandom;

public class LoggingRequestInterceptor extends HandlerInterceptorAdapter {

    private final String activeSpringProfile;

    public LoggingRequestInterceptor(Environment env) {
        String[] activeProfiles = env.getActiveProfiles();
        String activeProfile = "development";
        if (activeProfiles.length > 0) {
            activeProfile = activeProfiles[0];
        }
        this.activeSpringProfile = activeProfile;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        MDC.put("environment", activeSpringProfile);
        MDC.put("request_id", String.valueOf(ThreadLocalRandom.current().nextInt()));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AuthenticatedUser) {
            User user = ((AuthenticatedUser) authentication).getUser();
            MDC.put("user_id", String.valueOf(user.getId()));
            MDC.put("user_email", user.getEmail());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.clear();
    }
}