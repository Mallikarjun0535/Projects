package com.dizzion.portal.infra.email;

import com.dizzion.portal.infra.messaging.EmailQueueMessage;
import com.dizzion.portal.infra.template.TemplateService;
import com.dizzion.portal.infra.template.TemplateService.EmailTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.dizzion.portal.infra.template.TemplateService.EmailTemplate.EMERGENCY;
import static org.springframework.util.StringUtils.hasText;

@Component
@Slf4j
public class EmailSender {

    private final JavaMailSender generalMailSender;
    private final JavaMailSender emergencyMailSender;
    private final MimeMessageHelperFactory messageHelperFactory;
    private final TemplateService templateService;

    public EmailSender(JavaMailSender generalMailSender,
                       @Qualifier("emergencyMailSender") JavaMailSender emergencyMailSender,
                       MimeMessageHelperFactory messageHelperFactory,
                       TemplateService templateService) {
        this.generalMailSender = generalMailSender;
        this.emergencyMailSender = emergencyMailSender;
        this.messageHelperFactory = messageHelperFactory;
        this.templateService = templateService;
    }

    @JmsListener(destination = "${aws.sqs.email.queue-name}")
    public void sendEmails(EmailQueueMessage[] messages) {
        try {
            if (messages.length > 0) {
                JavaMailSender sender = messages[0].getTemplate() == EMERGENCY ? emergencyMailSender : generalMailSender;
                sender.send(generateMimeMessages(messages));
            } else {
                log.warn("Email sender received an empty message array");
            }
        } catch (MailSendException sendException) {
            log.warn("Cannot send some emails: {}", sendException.getFailedMessages(), sendException);
        }
    }

    private MimeMessage[] generateMimeMessages(EmailQueueMessage[] queueMessages) {
        List<MimeMessage> messages = new ArrayList<>();
        for (EmailQueueMessage msg : queueMessages) {
            for (String address : msg.getRecipients()) {
                if (!hasText(address)) {
                    continue;
                }
                String htmlContent = templateService.processEmail(msg.getTemplate(), msg.getTemplateParams());
                try {
                    messages.add(createMimeMessage(address, htmlContent, msg.getTemplate()));
                } catch (MessagingException | UnsupportedEncodingException ex) {
                    log.error("Can't create MIME message", ex);
                }
            }
        }
        return messages.toArray(new MimeMessage[messages.size()]);
    }

    private MimeMessage createMimeMessage(String email, String content, EmailTemplate template) throws MessagingException, UnsupportedEncodingException {
        MimeMessageHelper mimeMessageHelper = messageHelperFactory.createMessageHelper(template);
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject(template.getSubject());
        mimeMessageHelper.setText(content, true);
        return mimeMessageHelper.getMimeMessage();
    }
}

