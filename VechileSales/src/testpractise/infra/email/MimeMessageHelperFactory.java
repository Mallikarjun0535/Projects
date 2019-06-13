package com.dizzion.portal.infra.email;

import com.dizzion.portal.infra.template.TemplateService.EmailTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

import static com.dizzion.portal.infra.template.TemplateService.EmailTemplate.EMERGENCY;

@Component
public class MimeMessageHelperFactory {

    private final String generalFrom;
    private final String emergencyFrom;
    private final String fromName;
    private final JavaMailSender generalMailSender;
    private final JavaMailSender emergencyMailSender;

    public MimeMessageHelperFactory(@Value("${spring.mail.general.username}") String generalFrom,
                                    @Value("${spring.mail.emergency.username}") String emergencyFrom,
                                    @Value("${spring.mail.from-name}") String fromName,
                                    JavaMailSender generalMailSender,
                                    @Qualifier("emergencyMailSender") JavaMailSender emergencyMailSender) {
        this.generalFrom = generalFrom;
        this.emergencyFrom = emergencyFrom;
        this.fromName = fromName;
        this.generalMailSender = generalMailSender;
        this.emergencyMailSender = emergencyMailSender;
    }

    public MimeMessageHelper createMessageHelper(EmailTemplate template) throws MessagingException, UnsupportedEncodingException {
        MimeMessageHelper message;
        if (template == EMERGENCY) {
            MimeMessage mimeMessage = this.emergencyMailSender.createMimeMessage();
            message = new MimeMessageHelper(mimeMessage);
            message.setFrom(emergencyFrom, fromName);
        } else {
            MimeMessage mimeMessage = this.generalMailSender.createMimeMessage();
            message = new MimeMessageHelper(mimeMessage);
            message.setFrom(generalFrom, fromName);
        }
        return message;
    }
}
