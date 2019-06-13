package com.dizzion.portal.domain.maintenance;

import com.dizzion.portal.domain.common.DateFormatService;
import com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent;
import com.dizzion.portal.domain.organization.dto.Organization;
import com.dizzion.portal.domain.user.persistence.UserRepository;
import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import com.dizzion.portal.infra.messaging.EmailQueueMessage;
import com.dizzion.portal.infra.messaging.MessageQueues;
import com.dizzion.portal.security.auth.token.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Predicate;

import static com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent.ProgressStatus;
import static com.dizzion.portal.domain.role.Permission.VIEW_MAINTENANCE_EVENTS;
import static com.dizzion.portal.domain.role.dto.Role.*;
import static com.dizzion.portal.infra.template.TemplateService.EmailTemplate.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.*;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Service
@Transactional
public class MaintenanceEventEmailService {

    private final static Set<String> APPROVAL_REQUESTS_RECIPIENT_ROLES = ImmutableSet.of(
            PORTAL_ADMIN,
            PARTNER_ADMIN,
            ORGANIZATION_ADMIN
    );

    private final String frontendUrl;
    private final String scheduledJobsReportRecipient;
    private final String scheduledJobsUnassignedResourceNotificationRecipient;
    private final MessageQueues messageQueues;
    private final UserRepository userRepo;
    private final TokenService tokenService;
    private final DateFormatService dateFormatService;
    private final ObjectMapper objectMapper;

    public MaintenanceEventEmailService(@Value("${dizzion.frontend.url}") String frontendUrl,
                                        @Value("${scheduled-jobs.report.recipient}") String scheduledJobsReportRecipient,
                                        @Value("${scheduled-jobs.unassigned-resource.recipient}") String scheduledJobsUnassignedResourceNotificationRecipient,
                                        MessageQueues messageQueues,
                                        UserRepository userRepo,
                                        TokenService tokenService,
                                        DateFormatService dateFormatService, ObjectMapper objectMapper) {
        this.frontendUrl = frontendUrl;
        this.scheduledJobsReportRecipient = scheduledJobsReportRecipient;
        this.scheduledJobsUnassignedResourceNotificationRecipient = scheduledJobsUnassignedResourceNotificationRecipient;
        this.messageQueues = messageQueues;
        this.userRepo = userRepo;
        this.tokenService = tokenService;
        this.dateFormatService = dateFormatService;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public void sendNotifications(MaintenanceEvent event) {
        messageQueues.enqueueEmail(new EmailQueueMessage(MAINTENANCE_NOTIFICATION,
                ImmutableMap.of(
                        "jobTitle", event.getTitle(),
                        "type", event.getType().getHumanReadableName(),
                        "startDate", dateFormatService.formatDateTime(event.getStartDateTime()),
                        "endDate", dateFormatService.formatDateTime(event.getEndDateTime()),
                        "eventLink", urlFor(event)),
                getEmails(getIds(event.getOrganizations())))
        );
    }

    @Transactional(readOnly = true)
    public void sendApprovalRequests(MaintenanceEvent event, Set<Organization> orgs, String recurrencePatternName) {
        EmailQueueMessage[] messages = userRepo.findByOrganizationIdInAndRoleNameIn(
                asList(getIds(orgs)),
                APPROVAL_REQUESTS_RECIPIENT_ROLES)
                .stream()
                .map(user -> new EmailQueueMessage(MAINTENANCE_APPROVAL,
                        ImmutableMap.<String, Object>builder()
                                .put("jobTitle", event.getTitle())
                                .put("type", event.getType().getHumanReadableName())
                                .put("startDate", dateFormatService.formatDateTime(event.getStartDateTime()))
                                .put("endDate", dateFormatService.formatDateTime(event.getEndDateTime()))
                                .put("recurrencePatternName", recurrencePatternName)
                                .put("repeatUntil", event.getRepeatUntil()
                                        .map(date -> dateFormatService.formatDateTime(date.atStartOfDay(event.getStartDateTime().getZone())))
                                        .orElse(""))
                                .put("description", event.getMessage())
                                .put("eventLink", urlFor(event))
                                .put("approveUrl", generateApprovalLink(event, user.getId(), true))
                                .put("rejectUrl", generateApprovalLink(event, user.getId(), false))
                                .build(),
                        singleton(user.getEmail())))
                .toArray(EmailQueueMessage[]::new);

        messageQueues.enqueueEmail(messages);
    }

    @Transactional(readOnly = true)
    public void sendProgressNotifications(MaintenanceEvent event) {
        ProgressStatus progressStatus = event.getProgressStatus().orElseThrow(IllegalArgumentException::new);
        messageQueues.enqueueEmail(new EmailQueueMessage(MAINTENANCE_PROGRESS,
                ImmutableMap.<String, Object>builder()
                        .put("jobTitle", event.getTitle())
                        .put("type", event.getType().getHumanReadableName())
                        .put("startDate", dateFormatService.formatDateTime(event.getStartDateTime()))
                        .put("endDate", dateFormatService.formatDateTime(event.getEndDateTime()))
                        .put("progressStatus", progressStatus)
                        .put("eventLink", urlFor(event))
                        .build(),
                getEmails(getIds(event.getOrganizations()))));
    }

    @Transactional(readOnly = true)
    public void sendReport(List<MaintenanceEvent> yesterdayEvents, List<MaintenanceEvent> todayEvents) {
        messageQueues.enqueueEmail(new EmailQueueMessage(SCHEDULED_JOBS_REPORT,
                ImmutableMap.<String, Object>builder()
                        .put("yesterdayEvents", getMaintenanceEventsReportInfo(yesterdayEvents))
                        .put("todayEvents", getMaintenanceEventsReportInfo(todayEvents))
                        .build(), singleton(scheduledJobsReportRecipient)));
    }

    public void sendUnassignedResourceNotification(MaintenanceEvent event) {
        messageQueues.enqueueEmail(new EmailQueueMessage(SCHEDULED_JOBS_UNASSIGNED_RESOURCE,
                ImmutableMap.of(
                        "jobTitle", event.getTitle(),
                        "type", event.getType().getHumanReadableName(),
                        "startDate", dateFormatService.formatDateTime(event.getStartDateTime()),
                        "eventLink", urlFor(event)), singleton(scheduledJobsUnassignedResourceNotificationRecipient)));
    }

    public String urlFor(MaintenanceEvent event) {
        return fromHttpUrl(frontendUrl)
                .pathSegment("console", "dashboard", "maintenance", String.valueOf(event.getId()))
                .queryParam("startDate", event.getStartDateTime().toLocalDate())
                .build().toUriString();
    }

    public Set<String> getAdminEmails(Long... orgIds) {
        return extractEmails(asList(orgIds), user -> APPROVAL_REQUESTS_RECIPIENT_ROLES.contains(user.getRole().getName()));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getMaintenanceEventsReportInfo(List<MaintenanceEvent> events) {
        return events.stream()
                .sorted(Comparator.comparing(MaintenanceEvent::getStartDateTime))
                .map(event -> {
                    Map<String, Object> eventProperties = objectMapper.convertValue(event, Map.class);
                    eventProperties.put("eventLink", urlFor(event));
                    eventProperties.put("time", dateFormatService.formatTime(event.getStartDateTime()) + " - " + dateFormatService.formatTime(event.getEndDateTime()));
                    eventProperties.put("orgNames", event.getOrganizations().stream().map(Organization::getName).collect(joining(", ")));
                    return eventProperties;
                }).collect(toList());
    }

    private Set<String> getEmails(Long... orgIds) {
        return extractEmails(asList(orgIds), user -> user.getRole().getPermissions().contains(VIEW_MAINTENANCE_EVENTS));
    }

    private Long[] getIds(Collection<Organization> organizations) {
        return organizations.stream().map(Organization::getId).toArray(Long[]::new);
    }

    private Set<String> extractEmails(Collection<Long> orgIds, Predicate<UserEntity> userFilter) {
        return userRepo.findByOrganizationIdIn(orgIds).stream()
                .filter(userFilter)
                .map(UserEntity::getEmail)
                .collect(toSet());
    }

    private String generateApprovalLink(MaintenanceEvent event, long userId, boolean approve) {
        return fromHttpUrl(frontendUrl)
                .pathSegment("api", "maintenance-events", String.valueOf(event.getId()), "external-approval")
                .queryParam("token", tokenService.generateExternalToken(userId))
                .queryParam("approve", approve).build().toUriString();

    }
}
