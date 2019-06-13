package com.dizzion.portal.infra.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import static com.amazon.sqs.javamessaging.SQSMessagingClientConstants.JMSX_GROUP_ID;

@Component
public class MessageQueues {
    private final JmsTemplate jmsTemplate;
    private final String emailQueueName;
    private final String smsQueueName;

    public MessageQueues(JmsTemplate jmsTemplate,
                         @Value("${aws.sqs.email.queue-name}") String emailQueueName,
                         @Value("${aws.sqs.sms.queue-name}") String smsQueueName) {
        this.jmsTemplate = jmsTemplate;
        this.emailQueueName = emailQueueName;
        this.smsQueueName = smsQueueName;
    }


    public void enqueueEmail(EmailQueueMessage... messages) {
        if (messages.length == 0) {
            return;
        }
        jmsTemplate.convertAndSend(emailQueueName, messages, msg -> {
            msg.setStringProperty(JMSX_GROUP_ID, "email");
            return msg;
        });
    }

    public void enqueueSms(SmsQueueMessage message) {
        jmsTemplate.convertAndSend(smsQueueName, message, msg -> {
            msg.setStringProperty(JMSX_GROUP_ID, "sms");
            return msg;
        });
    }
}
