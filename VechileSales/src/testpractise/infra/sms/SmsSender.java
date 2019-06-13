package com.dizzion.portal.infra.sms;

import com.dizzion.portal.infra.messaging.SmsQueueMessage;
import lombok.AllArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SmsSender {
    private final TwilioClient twilioClient;

    @JmsListener(destination = "${aws.sqs.sms.queue-name}")
    public void sendSMS(SmsQueueMessage message) {
        twilioClient.sendSMS(message.getPhoneNumbers(), message.getContent());
    }
}
