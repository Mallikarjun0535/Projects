package com.dizzion.portal.infra.messaging;

import com.dizzion.portal.infra.template.TemplateService.EmailTemplate;
import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.Map;
import java.util.Set;

@Value
@NonFinal
public class EmailQueueMessage {
    EmailTemplate template;
    Map<String, Object> templateParams;
    Set<String> recipients;
}
