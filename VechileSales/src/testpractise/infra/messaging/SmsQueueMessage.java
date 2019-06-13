package com.dizzion.portal.infra.messaging;

import lombok.Value;

import java.util.Set;

@Value
public class SmsQueueMessage {
    Set<String> phoneNumbers;
    String content;
}
