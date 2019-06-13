package com.dizzion.portal.domain.maintenance;

import com.dizzion.portal.domain.common.DateFormatService;
import com.dizzion.portal.domain.helpdesk.ConnectWiseCompanyService;
import com.dizzion.portal.domain.helpdesk.ConnectWiseTicketingService;
import com.dizzion.portal.domain.helpdesk.dto.*;
import com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent;
import com.dizzion.portal.domain.maintenance.persistence.MaintenanceEventInstanceRepository;
import com.dizzion.portal.domain.maintenance.persistence.MaintenanceEventProgressTicketRepository;
import com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventEntity;
import com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventInstanceEntity;
import com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventProgressTicketEntity;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.user.UserService;
import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import com.google.common.collect.ImmutableSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static com.dizzion.portal.domain.common.ExceptionUtils.optionalOnException;
import static com.dizzion.portal.domain.common.ExceptionUtils.tolerateHttpExceptions;
import static com.dizzion.portal.domain.helpdesk.ConnectWiseTicketingService.MAX_SUMMARY_LENGTH;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Board.CUSTOMER_PORTAL;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Status.*;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Type.CUSTOMER_PORTAL_MWIN;
import static com.dizzion.portal.domain.helpdesk.dto.TicketCommentCreateUpdateRequest.discussionComment;
import static com.google.common.collect.Iterables.getFirst;
import static java.lang.String.format;
import static java.time.ZonedDateTime.now;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.removeStart;

@Service
@Transactional
public class MaintenanceTicketService {
    private static final String MAINTENANCE_UPDATE_COMMENT_PREFIX = "[Maintenance update]\n";

    private final AuthenticatedUserAccessor auth;
    private final ConnectWiseTicketingService connectWiseTicketingService;
    private final ConnectWiseCompanyService connectWiseCompanyService;
    private final MaintenanceEventEmailService maintenanceEventEmailService;
    private final MaintenanceEventInstanceRepository maintenanceInstanceRepo;
    private final MaintenanceEventProgressTicketRepository progressTicketRepo;
    private final UserService userService;
    private final Set<String> additionalCcEmails;
    private final DateFormatService dateFormatService;

    public MaintenanceTicketService(AuthenticatedUserAccessor auth, ConnectWiseTicketingService connectWiseTicketingService,
                                    ConnectWiseCompanyService connectWiseCompanyService,
                                    MaintenanceEventEmailService maintenanceEventEmailService,
                                    MaintenanceEventInstanceRepository maintenanceInstanceRepo, MaintenanceEventProgressTicketRepository progressTicketRepo,
                                    UserService userService,
                                    @Value("${maintenance.ticket.additional-cc-emails}") Set<String> additionalCcEmails,
                                    DateFormatService dateFormatService) {
        this.auth = auth;
        this.connectWiseTicketingService = connectWiseTicketingService;
        this.connectWiseCompanyService = connectWiseCompanyService;
        this.maintenanceEventEmailService = maintenanceEventEmailService;
        this.maintenanceInstanceRepo = maintenanceInstanceRepo;
        this.progressTicketRepo = progressTicketRepo;
        this.userService = userService;
        this.additionalCcEmails = additionalCcEmails;
        this.dateFormatService = dateFormatService;
    }

    public long createApprovalTicket(MaintenanceEventEntity event, String recurrencePatternName) {
        if (!connectWiseCompanyService.getContact(getCurrentUser()).isPresent()) {
            connectWiseCompanyService.createContact(getCurrentUser().getId());
        }

        Set<String> ccEmails = new HashSet<>(additionalCcEmails);
        event.getAssignedUser().ifPresent(user -> ccEmails.add(user.getEmail()));

        TicketCreateRequest ticket = TicketCreateRequest.builder()
                .summary(maintenanceTicketSummary(event))
                .severity(ConnectWiseTicket.Severity.valueOf(event.getSeverity().toString()))
                .type(CUSTOMER_PORTAL_MWIN)
                .detailDescription(maintenanceTicketDescription(event, recurrencePatternName))
                .ccEmails(ccEmails)
                .boardId(CUSTOMER_PORTAL.getId())
                .build();
        return connectWiseTicketingService.createTicket(ticket).getId();
    }

    public long createApprovalTicket(MaintenanceEventEntity event, MultipartFile[] cref, String recurrencePatternName) {
        long ticketId = createApprovalTicket(event, recurrencePatternName);
        Arrays.stream(cref).forEach(file -> connectWiseTicketingService.uploadDocument(ticketId, file));
        //connectWiseTicketingService.uploadDocument(ticketId, file);
        return ticketId;
    }

    public void closeApprovalTicket(MaintenanceEventEntity event, String comment) {
        event.getApprovalTicketId().ifPresent(ticketId -> {
            commentTicket(ticketId, discussionComment(comment));
            updateTicketStatus(ticketId, CUSTOMER_PORTAL_CLOSED);
        });
    }

    public void commentApprovalTicket(MaintenanceEventEntity eventEntity, TicketCommentCreateUpdateRequest comment) {
        eventEntity.getApprovalTicketId().ifPresent(ticketId -> commentTicket(ticketId, comment));
    }

    public void createProgressTickets(MaintenanceEvent maintenanceEvent) {
        String type = maintenanceEvent.getType().getHumanReadableName();
        maintenanceEvent.getOrganizations().forEach(org -> {
            String summary = "Scheduled " + type + " has been started as of " + dateFormatService.formatDateTime(now());
            String description = "Scheduled " + type + " window has been started as of " + dateFormatService.formatDateTime(now()) + "\n" +
                    "View details: " + maintenanceEventEmailService.urlFor(maintenanceEvent) + "\n" +
                    "If you have any questions, please contact your Client Relations Manager or Service Delivery Manager.";

            User contact = maintenanceEvent.getAssignedUser()
                    .map(userInfo -> userService.getUser(userInfo.getId()))
                    .orElseGet(this::getCurrentUser);
            if (!connectWiseCompanyService.getContact(contact).isPresent()) {
                connectWiseCompanyService.createContact(contact.getId());
            }

            Set<String> ccEmails = ImmutableSet.<String>builder()
                    .addAll(maintenanceEventEmailService.getAdminEmails(org.getId()))
                    .addAll(additionalCcEmails)
                    .build();

            tolerateHttpExceptions(() -> {
                ConnectWiseTicket ticket = connectWiseTicketingService.createTicket(TicketCreateRequest.builder()
                        .contactEmail(contact.getEmail())
                        .organizationId(org.getId())
                        .status(CUSTOMER_PORTAL_IN_PROGRESS)
                        .summary(summary)
                        .detailDescription(description)
                        .severity(ConnectWiseTicket.Severity.valueOf(maintenanceEvent.getSeverity().toString()))
                        .type(CUSTOMER_PORTAL_MWIN)
                        .ccEmails(ccEmails)
                        .boardId(CUSTOMER_PORTAL.getId())
                        .build());
                long maintenanceEventId = getMaintenanceEventInstanceId(maintenanceEvent)
                        .orElseThrow(() -> new IllegalStateException("Maintenance event instance not found"));
                progressTicketRepo.save(new MaintenanceEventProgressTicketEntity(maintenanceEventId, org.getId(), ticket.getId()));
            });
        });
    }

    public void finishProgressTickets(MaintenanceEvent maintenanceEvent) {
        String type = maintenanceEvent.getType().getHumanReadableName();
        String text = "Scheduled  " + type + " window has been completed as of " + dateFormatService.formatDateTime(now());
        TicketCommentCreateUpdateRequest comment = discussionComment(markComment(text));
        updateProgressTicketsStatus(maintenanceEvent, CUSTOMER_PORTAL_COMPLETED, comment);

    }

    public void failProgressTickets(MaintenanceEvent maintenanceEvent, String failureReason) {
        String type = maintenanceEvent.getType().getHumanReadableName();
        String text = "Scheduled " + type + " has not been completed successfully. " +
                "The following information has been provided regarding the failure:\n" + failureReason;
        updateProgressTicketsStatus(maintenanceEvent, CUSTOMER_PORTAL_COMPLETED, discussionComment(markComment(text)));
    }

    public void assignUserToTickets(MaintenanceEvent maintenanceEvent, String email) {
        tolerateHttpExceptions(() -> {
            maintenanceEvent.getApprovalTicketId().ifPresent(ticketId -> {
                List<String> ccEmails = connectWiseTicketingService.getCcEmails(ticketId);
                if (!ccEmails.contains(email)) {
                    HashSet<String> newCcs = new HashSet<>(ccEmails);
                    newCcs.add(email);
                    connectWiseTicketingService.updateTicket(ticketId, TicketUpdateRequest.builder()
                            .ccEmails(newCcs)
                            .build());
                }
            });
            getProgressTicketIds(maintenanceEvent).forEach(ticketId ->
                    connectWiseTicketingService.updateTicket(ticketId, TicketUpdateRequest.builder()
                            .contactEmail(email)
                            .build())
            );
        });
    }

    public List<TicketComment> getProgressTicketComments(MaintenanceEvent maintenanceEvent) {
        Long firstTicketId = getFirst(getProgressTicketIds(maintenanceEvent), null);
        return Optional.ofNullable(firstTicketId)
                .flatMap(ticketId -> optionalOnException(() -> connectWiseTicketingService.getTicketDetails(ticketId)))
                .map(ticketDetails -> getMarkedComments(ticketDetails.getComments()))
                .orElse(emptyList());
    }

    public void commentProgressTickets(MaintenanceEvent maintenanceEvent, TicketCommentCreateUpdateRequest comment, Optional<MultipartFile> attachment) {
        getProgressTicketIds(maintenanceEvent).forEach(ticketId -> tolerateHttpExceptions(() -> {
            String text = attachment
                    .map(file -> markAttachment(comment.getText(), file.getOriginalFilename()))
                    .map(this::markComment)
                    .orElseGet(() -> markComment(comment.getText()));
            connectWiseTicketingService.createTimeEntryWithAuthor(ticketId, comment.toBuilder()
                    .text(text)
                    .build());
            attachment.ifPresent(file -> connectWiseTicketingService.uploadDocument(ticketId, file));
        }));
    }

    public List<TicketAttachment> getProgressTicketAttachments(MaintenanceEvent maintenanceEvent) {
        Long firstTicketId = getFirst(getProgressTicketIds(maintenanceEvent), null);

        return Optional.ofNullable(firstTicketId)
                .flatMap(ticketId -> optionalOnException(() -> connectWiseTicketingService.getTicketDetails(ticketId)))
                .map(TicketDetails::getAttachments)
                .orElse(emptyList());
    }

    public Map<Long, Long> getProgressTicketIdsByOrganizationId(long maintenanceEventInstanceId) {
        return progressTicketRepo.findByMaintenanceEventInstanceId(maintenanceEventInstanceId).stream()
                .collect(toMap(
                        MaintenanceEventProgressTicketEntity::getOrganizationId,
                        MaintenanceEventProgressTicketEntity::getTicketId));
    }

    private List<TicketComment> getMarkedComments(List<TicketComment> comments) {
        return comments.stream()
                .filter(comment -> comment.getText().startsWith(MAINTENANCE_UPDATE_COMMENT_PREFIX))
                .map(comment -> comment.toBuilder()
                        .text(removeStart(comment.getText(), MAINTENANCE_UPDATE_COMMENT_PREFIX))
                        .build())
                .collect(toList());
    }

    private String markAttachment(String comment, String fileName) {
        return "[Attachment: " + fileName + "]\n\n" + comment;
    }

    private String markComment(String text) {
        return MAINTENANCE_UPDATE_COMMENT_PREFIX + text;
    }

    private void updateProgressTicketsStatus(MaintenanceEvent maintenanceEvent, ConnectWiseTicket.Status status, TicketCommentCreateUpdateRequest comment) {
        getProgressTicketIds(maintenanceEvent).forEach(ticketId -> {
            commentTicket(ticketId, comment);
            updateTicketStatus(ticketId, status);
        });
    }

    private Set<Long> getProgressTicketIds(MaintenanceEvent maintenanceEvent) {
        return getMaintenanceEventInstanceId(maintenanceEvent)
                .map(progressTicketRepo::findByMaintenanceEventInstanceId)
                .orElse(emptyList()).stream()
                .map(MaintenanceEventProgressTicketEntity::getTicketId)
                .collect(toSet());
    }

    private void commentTicket(long ticketId, TicketCommentCreateUpdateRequest comment) {
        tolerateHttpExceptions(() -> connectWiseTicketingService.createTimeEntry(ticketId, comment));
    }

    private void updateTicketStatus(Long ticketId, ConnectWiseTicket.Status status) {
        tolerateHttpExceptions(() ->
                connectWiseTicketingService.updateTicket(ticketId, TicketUpdateRequest.builder().statusName(status).build()));
    }

    private Optional<Long> getMaintenanceEventInstanceId(MaintenanceEvent maintenanceEvent) {
        return maintenanceInstanceRepo
                .findByMaintenanceEventIdAndStartDateTime(maintenanceEvent.getId(), maintenanceEvent.getStartDateTime())
                .map(MaintenanceEventInstanceEntity::getId);
    }

    private String maintenanceTicketSummary(MaintenanceEventEntity event) {
        String type = event.getType().getHumanReadableName();
        String startTime = dateFormatService.formatDateTime(event.getStartDateTime());
        String endTime = dateFormatService.formatDateTime(event.getEndDateTime());
        String summary = format("%s from %s to %s", type, startTime, endTime);

        String summaryWithOrgs = organizationListString(event, ", ") + " " + summary;
        return summaryWithOrgs.length() <= MAX_SUMMARY_LENGTH
                ? summaryWithOrgs
                : capitalize(summary);
    }

    private String maintenanceTicketDescription(MaintenanceEventEntity event, String recurrencePatternName) {
        return event.getTitle() + "\n\n" +
                (event.isRecurring() ? "Recurrence: " + recurrencePatternName + "\n\n" : "") +
                "Affected organizations:\n" + organizationListString(event, "\n") + "\n\n" +
                "View details: " + maintenanceEventEmailService.urlFor(MaintenanceEvent.from(event));
    }

    private String organizationListString(MaintenanceEventEntity event, String delimiter) {
        return event.getOrganizations().stream()
                .map(OrganizationEntity::getName)
                .collect(joining(delimiter));
    }

    private User getCurrentUser() {
        return auth.getAuthenticatedUser().getUser();

    }
}
