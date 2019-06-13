package com.dizzion.portal.infra.template;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class TemplateService {
    private final TemplateEngine emailEngine;
    private final TemplateEngine textEngine;

    public TemplateService(@Qualifier("email") TemplateEngine emailEngine,
                           @Qualifier("text") TemplateEngine textEngine) {
        this.emailEngine = emailEngine;
        this.textEngine = textEngine;
    }

    public String processEmail(EmailTemplate template, Map<String, Object> parameters) {
        Context context = new Context();
        context.setVariables(parameters);
        return emailEngine.process(template.getName(), context);
    }

    @SuppressWarnings("unchecked")
    public String processText(TextTemplate template, Map<String, String> parameters) {
        Context context = new Context();
        context.setVariables((Map) parameters);
        return textEngine.process(template.getName(), context);
    }

    @Getter
    @AllArgsConstructor
    public enum EmailTemplate {
        EMERGENCY("emergency", "Dizzion Control Center: Emergency Notification"),
        EMERGENCY_HISTORY("emergency-history", "Dizzion Control Center: Emergency Summary"),
        REGISTRATION("registration", "Dizzion Control Center: New User Registration"),
        RESET_PASSWORD("reset-password", "Dizzion Control Center: Reset password"),
        MAINTENANCE_PROGRESS("maintenance-progress", "Dizzion Control Center: Maintenance Progress Notification"),
        MAINTENANCE_APPROVAL("maintenance-approval", "Dizzion Control Center: Scheduled Maintenance Window Notification. Request for approval"),
        MAINTENANCE_NOTIFICATION("maintenance-notification", "Dizzion Control Center: Scheduled Maintenance Window Notification"),
        SCHEDULED_JOBS_REPORT("scheduled-jobs-report", "Dizzion Control Center: Scheduled Jobs Report"),
        SCHEDULED_JOBS_UNASSIGNED_RESOURCE("scheduled-jobs-unassigned-resource", "Action Required: No Assigned Resource for Upcoming Scheduled Job"),
        PIN("pin", "Dizzion Control Center: Customer PIN");

        private String name;
        private String subject;
    }

    @Getter
    @AllArgsConstructor
    public enum TextTemplate {
        OUTAGE_OPEN("outage-open"),
        OUTAGE_UPDATE("outage-update"),
        OUTAGE_CLOSE("outage-close"),
        DEGRADED_SERVICE_OPEN("degraded-service-open"),
        DEGRADED_SERVICE_UPDATE("degraded-service-update"),
        DEGRADED_SERVICE_CLOSE("degraded-service-close"),
        PATCHING_OPEN("patching-open"),
        PATCHING_UPDATE("patching-update"),
        PATCHING_CLOSE("patching-close"),
        GENERAL_OPEN("general-open"),
        GENERAL_UPDATE("general-update"),
        GENERAL_CLOSE("general-close");

        private String name;
    }
}
