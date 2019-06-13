package com.dizzion.portal.domain.monitoring.web;

import com.dizzion.portal.domain.cosmos.persistence.UtilizationReportingDao;
import com.dizzion.portal.domain.helpdesk.ConnectWiseSystemService;
import com.dizzion.portal.domain.monitoring.dto.MonitoringIndicator;
import com.dizzion.portal.infra.sms.TwilioClient;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.dizzion.portal.domain.role.Permission.Constants.MONITORING;

@RestController
@AllArgsConstructor
public class MonitoringController {

    private final ConnectWiseSystemService connectWiseSystemService;
    private final TwilioClient twilioClient;
    private final JavaMailSender mailSender;
    private final UtilizationReportingDao utilizationReportingDao;

    @GetMapping("/monitoring/connectwise")
    @Secured(MONITORING)
    public MonitoringIndicator getCwAvailability() {
        return new MonitoringIndicator(connectWiseSystemService.isAvailable());
    }

    @GetMapping("/monitoring/twilio")
    @Secured(MONITORING)
    public MonitoringIndicator getTwilioAvailability() {
        return new MonitoringIndicator(twilioClient.isAvailable());
    }

    @GetMapping("/monitoring/email-server")
    @Secured(MONITORING)
    public MonitoringIndicator getEmailServerAvailability() {
        try {
            ((JavaMailSenderImpl) mailSender).testConnection();
            return new MonitoringIndicator(true);
        } catch (Exception e) {
            return new MonitoringIndicator(false);
        }
    }

    @GetMapping("/monitoring/cosmos-db")
    @Secured(MONITORING)
    public MonitoringIndicator getCosmosDbAvailability() {
        return new MonitoringIndicator(utilizationReportingDao.isAvailable());
    }
}
