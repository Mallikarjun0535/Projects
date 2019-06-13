package com.dizzion.portal.domain.emergencycommunication;

import com.dizzion.portal.domain.common.DateFormatService;
import com.dizzion.portal.domain.emergencycommunication.dto.*;
import com.dizzion.portal.domain.emergencycommunication.persistence.EmergencyConversationRepository;
import com.dizzion.portal.domain.emergencycommunication.persistence.EmergencyMessageRepository;
import com.dizzion.portal.domain.emergencycommunication.persistence.entity.EmergencyConversationEntity;
import com.dizzion.portal.domain.emergencycommunication.persistence.entity.EmergencyMessageEntity;
import com.dizzion.portal.domain.exception.EntityNotFoundException;
import com.dizzion.portal.domain.helpdesk.ConnectWiseCompanyService;
import com.dizzion.portal.domain.helpdesk.ConnectWiseTicketingService;
import com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket;
import com.dizzion.portal.domain.helpdesk.dto.TicketCommentCreateUpdateRequest;
import com.dizzion.portal.domain.helpdesk.dto.TicketCreateRequest;
import com.dizzion.portal.domain.helpdesk.dto.TicketUpdateRequest;
import com.dizzion.portal.domain.organization.persistence.OrganizationGroupRepository;
import com.dizzion.portal.domain.organization.persistence.OrganizationRepository;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationGroupEntity;
import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.domain.user.dto.User.NotificationMethod;
import com.dizzion.portal.domain.user.persistence.UserRepository;
import com.dizzion.portal.infra.messaging.EmailQueueMessage;
import com.dizzion.portal.infra.messaging.MessageQueues;
import com.dizzion.portal.infra.messaging.SmsQueueMessage;
import com.dizzion.portal.infra.template.TemplateService;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static com.dizzion.portal.domain.common.ApplicationPropertiesUtils.splitEmails;
import static com.dizzion.portal.domain.common.ExceptionUtils.tolerateHttpExceptions;
import static com.dizzion.portal.domain.emergencycommunication.dto.EmergencyConversation.ConversationType.DEGRADED_SERVICE;
import static com.dizzion.portal.domain.emergencycommunication.dto.EmergencyConversation.ConversationType.GENERAL;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Board.CUSTOMER_PORTAL;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Severity.MEDIUM;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Status.CUSTOMER_PORTAL_CLOSED;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Type.CUSTOMER_PORTAL_BREAK_FIX;
import static com.dizzion.portal.domain.helpdesk.dto.TicketCommentCreateUpdateRequest.discussionComment;
import static com.dizzion.portal.domain.user.dto.User.NotificationMethod.EMAIL;
import static com.dizzion.portal.domain.user.dto.User.NotificationMethod.SMS;
import static com.dizzion.portal.infra.template.TemplateService.EmailTemplate.EMERGENCY;
import static com.dizzion.portal.infra.template.TemplateService.EmailTemplate.EMERGENCY_HISTORY;
import static com.google.common.collect.Sets.newHashSet;
import static java.time.ZonedDateTime.now;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.*;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@Transactional
public class EmergencyCommunicationService {

    private final static String OPENING_EMERGENCY_SMS_NOTIFICATION_HEADER = "Dizzion Emergency Notification. ";
    private static final String ADDITIONAL_SERVICE_EMAILS = "additionalServiceEmails";

    private final EmergencyConversationRepository conversationRepo;
    private final EmergencyMessageRepository messageRepo;
    private final OrganizationRepository orgRepo;
    private final OrganizationGroupRepository orgGroupRepo;
    private final UserRepository userRepo;
    private final MessageQueues messageQueues;
    private final TemplateService templateService;
    private final Set<String> additionalServiceEmails;
    private final DateFormatService dateFormatService;
    private final ConnectWiseCompanyService connectWiseCompanyService;
    private final ConnectWiseTicketingService connectWiseTicketingService;
    private final AuthenticatedUserAccessor auth;

    public EmergencyCommunicationService(EmergencyConversationRepository conversationRepo,
                                         EmergencyMessageRepository emergencyMessageRepository,
                                         OrganizationRepository orgRepo,
                                         OrganizationGroupRepository orgGroupRepo,
                                         UserRepository userRepo,
                                         MessageQueues messageQueues,
                                         TemplateService templateService,
                                         @Value("${emergency.message.additional-service-emails}") String additionalServiceEmails,
                                         DateFormatService dateFormatService,
                                         ConnectWiseCompanyService connectWiseCompanyService,
                                         ConnectWiseTicketingService connectWiseTicketingService, AuthenticatedUserAccessor auth) {
        this.conversationRepo = conversationRepo;
        this.messageRepo = emergencyMessageRepository;
        this.orgRepo = orgRepo;
        this.orgGroupRepo = orgGroupRepo;
        this.userRepo = userRepo;
        this.messageQueues = messageQueues;
        this.templateService = templateService;
        this.additionalServiceEmails = splitEmails(additionalServiceEmails);
        this.dateFormatService = dateFormatService;
        this.connectWiseCompanyService = connectWiseCompanyService;
        this.connectWiseTicketingService = connectWiseTicketingService;
        this.auth = auth;
    }

    @Transactional(readOnly = true)
    public List<EmergencyConversation> getLastConversations() {
        Pageable last20 = new PageRequest(0, 20, new Sort(DESC, "createdAt"));
        return conversationRepo.findAll(last20)
                .map(EmergencyConversation::from)
                .getContent();
    }

    @Transactional(readOnly = true)
    public List<EmergencyMessage> getConversationMessages(long conversationId) {
        return messageRepo.findByEmergencyConversationIdOrderByCreatedAtAsc(conversationId).stream()
                .map(EmergencyMessage::from)
                .collect(toList());
    }

    public EmergencyConversation createConversation(EmergencyConversationCreateRequest request) {
        Map<String, String> props = new HashMap<>();
        if (!additionalServiceEmails.isEmpty()) {
            props.put(ADDITIONAL_SERVICE_EMAILS, String.join(", ", additionalServiceEmails));
        }

        long ticketId = createTicket(request).getId();
        EmergencyConversationEntity conversationEntity = conversationRepo.save(EmergencyConversationEntity.builder()
                .recipients(emptySet())
                .recipientGroups(emptySet())
                .type(request.getType())
                .properties(props)
                .name(request.getName())
                .createdAt(now())
                .helpDeskTicketId(ticketId)
                .build());

        return EmergencyConversation.from(conversationEntity);
    }

    public EmergencyConversation updateConversation(long id, EmergencyConversationUpdateRequest request) {
        EmergencyConversationEntity conversationEntity = Optional.ofNullable(conversationRepo.findOne(id))
                .orElseThrow(EntityNotFoundException::new);

        if (conversationEntity.isClosed()) {
            throw new IllegalStateException("Updating closed conversation");
        }

        boolean wasPrivateConversation = isPrivateConversation(conversationEntity);

        Set<OrganizationEntity> recipients = newHashSet(orgRepo.findAll(request.getRecipientOrganizationIds()));
        conversationEntity.setRecipients(recipients);

        Set<OrganizationGroupEntity> recipientGroups = newHashSet(orgGroupRepo.findAll(request.getRecipientOrganizationGroupIds()));
        conversationEntity.setRecipientGroups(recipientGroups);

        conversationEntity.setName(request.getName());
        conversationEntity.setProperties(request.getProperties());

        if (!wasPrivateConversation && isPrivateConversation(conversationEntity)) {
            throw new IllegalStateException();
        }

        updateTicket(conversationEntity, request);

        if (wasPrivateConversation && !isPrivateConversation(conversationEntity)) {
            checkSeverityHasAllowedValue(conversationEntity, request);

            String message = templateService.processText(
                    conversationEntity.getType().getOpenMessageTemplate(),
                    conversationEntity.getProperties());
            messageRepo.save(EmergencyMessageEntity.builder()
                    .emergencyConversationId(conversationEntity.getId())
                    .content(message)
                    .createdAt(now())
                    .internal(false)
                    .build());

            Multimap<NotificationMethod, User> recipientsByMethod = extractRecipientsByMethod(conversationEntity);
            sendEmail(recipientsByMethod.get(EMAIL), request.getName()+"<br/> <hr>"+ message);
            sendSms(recipientsByMethod.get(SMS), message);
            commentTicket(conversationEntity, discussionComment(message));
        }

        return EmergencyConversation.from(conversationEntity);
    }

    public EmergencyConversation closeConversation(long id) {
        EmergencyConversationEntity conversationEntity = Optional.ofNullable(conversationRepo.findOne(id))
                .orElseThrow(EntityNotFoundException::new);
        if (conversationEntity.isClosed()) {
            return EmergencyConversation.from(conversationEntity);
        }

        ZonedDateTime closedAt = now();
        conversationEntity.setClosedAt(closedAt);

        String message = templateService.processText(conversationEntity.getType().getCloseMessageTemplate(),
                ImmutableMap.<String, String>builder()
                        .putAll(conversationEntity.getProperties())
                        .put("startDate", dateFormatService.formatDateTime(conversationEntity.getCreatedAt()))
                        .build()
        );
        boolean isMessageInternal = isPrivateConversation(conversationEntity);

        messageRepo.save(EmergencyMessageEntity.builder()
                .emergencyConversationId(conversationEntity.getId())
                .content(message)
                .createdAt(closedAt)
                .internal(isMessageInternal)
                .build());

        if (!isMessageInternal) {
            List<EmergencyMessageEntity> messages = messageRepo.findByEmergencyConversationIdAndInternalFalseOrderByCreatedAtAsc(id);
            if (!messages.isEmpty()) {
                String messageWithHistory = templateService.processEmail(EMERGENCY_HISTORY,
                        ImmutableMap.of("closeMessage", message, "messages", messages));
                Multimap<NotificationMethod, User> recipientsByMethod = extractRecipientsByMethod(conversationEntity);
                sendEmail(recipientsByMethod.get(EMAIL), messageWithHistory);
                sendSms(recipientsByMethod.get(SMS), message);
            }
        }

        commentTicket(conversationEntity, discussionComment(isMessageInternal ? markInternal(message) : message));
        conversationEntity.getHelpDeskTicketId().ifPresent(ticketId ->
                connectWiseTicketingService.updateTicket(ticketId, TicketUpdateRequest.builder().statusName(CUSTOMER_PORTAL_CLOSED).build()));

        return EmergencyConversation.from(conversationEntity);
    }

    public EmergencyMessage sendUpdateMessage(long conversationId, EmergencyMessageSendRequest messageSendRequest) {
        EmergencyConversationEntity conversationEntity = Optional.ofNullable(conversationRepo.findOne(conversationId))
                .orElseThrow(EntityNotFoundException::new);

        if (conversationEntity.isClosed()) {
            throw new IllegalStateException("Sending message to a closed conversation");
        }

        EmergencyMessageEntity messageEntity;
        if (messageSendRequest.getInternal() || isPrivateConversation(conversationEntity)) {
            String message = messageSendRequest.getContent();
            messageEntity = messageRepo.save(EmergencyMessageEntity.builder()
                    .emergencyConversationId(conversationEntity.getId())
                    .content(message)
                    .internal(true)
                    .createdAt(now())
                    .build());

            sendInternalMessage(message);
            commentTicket(conversationEntity, discussionComment(markInternal(message)));
        } else {
            String message = templateService.processText(conversationEntity.getType().getUpdateMessageTemplate(),
                    ImmutableMap.<String, String>builder()
                            .putAll(conversationEntity.getProperties())
                            .put("startDate", dateFormatService.formatDateTime(conversationEntity.getCreatedAt()))
                            .put("message", messageSendRequest.getContent())
                            .build());
            messageEntity = messageRepo.save(EmergencyMessageEntity.builder()
                    .emergencyConversationId(conversationEntity.getId())
                    .content(message)
                    .internal(false)
                    .createdAt(now())
                    .build());

            Multimap<NotificationMethod, User> recipientsByMethod = extractRecipientsByMethod(conversationEntity);
            sendEmail(recipientsByMethod.get(EMAIL), message);
            String messageWithoutHtml = Jsoup.parse(message).text();
            sendSms(recipientsByMethod.get(SMS), messageWithoutHtml);
            commentTicket(conversationEntity, discussionComment(messageWithoutHtml));
        }
        return EmergencyMessage.from(messageEntity);
    }

    private ConnectWiseTicket createTicket(EmergencyConversationCreateRequest request) {
        User user = auth.getAuthenticatedUser().getUser();
        if (!connectWiseCompanyService.getContact(user).isPresent()) {
            connectWiseCompanyService.createContact(user.getId());
        }
        TicketCreateRequest ticket = TicketCreateRequest.builder()
                .summary(request.getName())
                .detailDescription("New emergency conversation started. Type: " + request.getType())
                .severity(MEDIUM)
                .type(CUSTOMER_PORTAL_BREAK_FIX)
                .boardId(CUSTOMER_PORTAL.getId())
                .build();
        return connectWiseTicketingService.createTicket(ticket);
    }

    private void updateTicket(EmergencyConversationEntity conversationEntity, EmergencyConversationUpdateRequest request) {
        conversationEntity.getHelpDeskTicketId().ifPresent(ticketId -> {
            ConnectWiseTicket.Severity severity = Optional.ofNullable(request.getProperties().get("severity"))
                    .map(ConnectWiseTicket.Severity::valueOf)
                    .orElse(MEDIUM);

            connectWiseTicketingService.updateTicket(ticketId, TicketUpdateRequest.builder()
                    .summary(request.getName())
                    .severity(severity)
                    .build());

            Set<OrganizationEntity> affectedOrgs = extractRecipientOrganizations(conversationEntity);
            if (!affectedOrgs.isEmpty()) {
                commentTicket(conversationEntity, discussionComment("Affected organizations:\n" + affectedOrgs.stream()
                        .map(org -> org.getName() + " - " + org.getCustomerId())
                        .collect(joining("\n"))));
            }
        });
    }

    private void commentTicket(EmergencyConversationEntity conversationEntity, TicketCommentCreateUpdateRequest commentRequest) {
        conversationEntity.getHelpDeskTicketId().ifPresent(id -> tolerateHttpExceptions(() ->
                connectWiseTicketingService.createTimeEntryWithAuthor(id, commentRequest)
        ));
    }

    private String markInternal(String message) {
        return "[internal only]\n" + Jsoup.parse(message).text();
    }

    private void sendEmail(Collection<User> recipients, String message) {
        Set<String> emails = recipients.stream().map(User::getEmail).collect(toSet());
        emails.addAll(additionalServiceEmails);
        if (!emails.isEmpty()) {
            messageQueues.enqueueEmail(new EmailQueueMessage(EMERGENCY, ImmutableMap.of("content", message), emails));
        }
    }

    private void sendSms(Collection<User> recipients, String message) {
        Set<String> phones = recipients.stream()
                .filter(user -> user.getMobilePhoneNumber().isPresent())
                .map(user -> user.getMobilePhoneNumber().get())
                .collect(toSet());
        if (!phones.isEmpty()) {
            messageQueues.enqueueSms(new SmsQueueMessage(phones, OPENING_EMERGENCY_SMS_NOTIFICATION_HEADER + message));
        }
    }

    private void sendInternalMessage(String message) {
        messageQueues.enqueueEmail(new EmailQueueMessage(EMERGENCY, ImmutableMap.of("content", message), additionalServiceEmails));
    }

    private Multimap<NotificationMethod, User> extractRecipientsByMethod(EmergencyConversationEntity conversationEntity) {
        Multimap<NotificationMethod, User> recipients = ArrayListMultimap.create();

        Set<Long> orgIds = extractRecipientOrganizations(conversationEntity).stream()
                .map(OrganizationEntity::getId)
                .collect(toSet());
        userRepo.findByOrganizationIdIn(orgIds).stream()
                .map(User::from)
                .forEach(user -> user.getNotificationMethods().forEach(method -> recipients.put(method, user)));
        return recipients;
    }

    private Set<OrganizationEntity> extractRecipientOrganizations(EmergencyConversationEntity conversationEntity) {
        return Stream.concat(
                conversationEntity.getRecipients().stream(),
                conversationEntity.getRecipientGroups().stream()
                        .flatMap(orgGroup -> orgGroup.getOrganizations().stream()))
                .collect(toSet());
    }

    private boolean isPrivateConversation(EmergencyConversationEntity conversation) {
        return conversation.getRecipientGroups().isEmpty() && conversation.getRecipients().isEmpty();
    }

    private void checkSeverityHasAllowedValue(EmergencyConversationEntity conversation, EmergencyConversationUpdateRequest request) {
        if (conversation.getType() == DEGRADED_SERVICE || conversation.getType() == GENERAL) {
            String severity = request.getProperties().get("severity");
            SeverityLevel.valueOf(severity);
        }
    }
}
