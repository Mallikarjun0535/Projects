package com.dizzion.portal.domain.helpdesk;

import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.helpdesk.dto.*;
import com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.*;
import com.dizzion.portal.domain.helpdesk.dto.TicketCommentCreateUpdateRequest.TicketCommentCreateUpdateRequestBuilder;
import com.dizzion.portal.domain.organization.OrganizationService;
import com.dizzion.portal.domain.organization.dto.Organization;
import com.dizzion.portal.domain.organization.dto.ShortOrganizationInfo;
import com.dizzion.portal.domain.user.UserService;
import com.dizzion.portal.domain.user.dto.ShortUserInfo;
import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static com.dizzion.portal.domain.common.PageUtils.emptyPage;
import static com.dizzion.portal.domain.common.PageUtils.pageOf;
import static com.dizzion.portal.domain.filter.FieldFilter.LogicOperator.AND;
import static com.dizzion.portal.domain.filter.FieldFilter.Operator.EQUALS;
import static com.dizzion.portal.domain.filter.FieldFilter.Operator.IN;
import static com.dizzion.portal.domain.filter.FilterUtils.findFilter;
import static com.dizzion.portal.domain.helpdesk.ConnectWiseUtils.*;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Board.*;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Status.*;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseUpdateOperation.removeOp;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseUpdateOperation.replaceOp;
import static com.google.common.collect.Lists.reverse;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newHashSet;
import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.concurrent.CompletableFuture.*;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.concat;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.http.MediaType.parseMediaType;
import static org.springframework.util.StringUtils.hasText;

@Service
@Slf4j
public class ConnectWiseTicketingService {
    public static final String CID_FIELD = "company/identifier";
    public static final String SUMMARY_FIELD = "summary";
    public static final String STATUS_FIELD = "status";
    public static final int MAX_SUMMARY_LENGTH = 100;
    public static final int MAX_PAGE_SIZE = 1000;

    private static final String EMAIL_DELIMITER = ";";
    private static final String ID_FIELD = "id";
    private static final String ORGANIZATION_ID_FIELD = "organizationId";
    private static final String STATUS_NAME_FIELD = "status/name";
    private static final String SEVERITY_FIELD = "severity";
    private static final String IMPACT_FIELD = "impact";
    private static final String ENTERED_DATE_FROM_FIELD = "dateEnteredFrom";
    private static final String ENTERED_DATE_TO_FIELD = "dateEnteredTo";
    private static final String LAST_UPDATED_FROM_FIELD = "lastUpdatedFrom";
    private static final String LAST_UPDATED_TO_FIELD = "lastUpdatedTo";
    private static final String BOARD_ID_FIELD = "board/id";
    private static final String TYPE_NAME_FIELD = "type/name";
    private static final String PRIORITY_ID_FIELD = "priority/id";
    private static final Set<Status> TICKET_CLOSED_STATUSES = ImmutableSet.of(
            CUSTOMER_EXPERIENCE_COMPLETED,
            CUSTOMER_EXPERIENCE_CLOSED,
            CUSTOMER_EXPERIENCE_CANCELLED,
            CUSTOMER_EXPERIENCE_CLOSED_DUPLICATE_NO_NOTIFICATION,
            CUSTOMER_PORTAL_COMPLETED,
            CUSTOMER_PORTAL_CLOSED,
            CUSTOMER_PORTAL_CANCELLED,
            CUSTOMER_PORTAL_CLOSED_DUPLICATE_NO_NOTIFICATION,
            PROVISIONING_CLOSED,
            CHANGE_MANAGEMENT_CLOSED,
            CLIENT_RELATIONS_COMPLETED,
            CLIENT_RELATIONS_CLOSED,
            PLATFORM_CLOSED);
    private static final String TICKET_OPEN_STATUSES_STRING = stream(Status.values())
            .filter(status -> !TICKET_CLOSED_STATUSES.contains(status))
            .map(Status::name)
            .collect(joining(","));
    private static final Set<String> SUPPORTED_FILTERS = ImmutableSet.of(
            ORGANIZATION_ID_FIELD,
            ENTERED_DATE_FROM_FIELD,
            ENTERED_DATE_TO_FIELD,
            LAST_UPDATED_FROM_FIELD,
            LAST_UPDATED_TO_FIELD,
            STATUS_FIELD,
            SEVERITY_FIELD,
            IMPACT_FIELD,
            SUMMARY_FIELD,
            BOARD_ID_FIELD,
            ID_FIELD,
            TYPE_NAME_FIELD,
            PRIORITY_ID_FIELD,
            "resources",
            "contactName");

    private final String ticketsUrl;
    private final String ticketUrl;
    private final String ticketNotesUrl;
    private final String ticketDocumentsUrl;
    private final String timeEntriesUrl;
    private final String scheduleEntriesUrl;
    private final String scheduleEntryUrl;
    private final String documentsUrl;
    private final String documentDownloadUrl;

    private final AuthenticatedUserAccessor auth;
    private final RestTemplate restTemplate;
    private final OrganizationService organizationService;
    private final UserService userService;
    private final ConnectWiseCompanyService cwCompanyService;

    public ConnectWiseTicketingService(@Value("${connectwise.service.base-url}") String baseUrl,
                                       @Qualifier("connectWiseRestTemplate") RestTemplate restTemplate,
                                       AuthenticatedUserAccessor auth,
                                       OrganizationService organizationService,
                                       UserService userService,
                                       ConnectWiseCompanyService cwCompanyService) {
        this.auth = auth;
        this.restTemplate = restTemplate;
        this.userService = userService;
        this.organizationService = organizationService;
        this.cwCompanyService = cwCompanyService;

        this.ticketsUrl = baseUrl + "service/tickets/";
        this.ticketUrl = baseUrl + "service/tickets/{id}";
        this.ticketNotesUrl = ticketUrl + "/notes";
        this.ticketDocumentsUrl = ticketUrl + "/documents";
        this.timeEntriesUrl = baseUrl + "time/entries";
        this.scheduleEntriesUrl = baseUrl + "schedule/entries";
        this.scheduleEntryUrl = baseUrl + "schedule/entries/{id}";
        this.documentsUrl = baseUrl + "system/documents";
        this.documentDownloadUrl = baseUrl + "system/documents/{id}/download";
    }

    public ConnectWiseTicket getTicket(long id) {
        return restTemplate.getForObject(ticketUrl, ConnectWiseTicket.class, id);
    }

    public List<ConnectWiseTicket> getTickets(FieldFilter... filters) {
        return getTicketsPage(newHashSet(filters), new PageRequest(0, MAX_PAGE_SIZE)).getContent();
    }

    public Page<ConnectWiseTicket> getTicketsPage(Set<FieldFilter> filters, Pageable pageRequest) {
        checkUnsupportedFilters(filters);
        filters = preprocessFilters(filters);

        if (!auth.isPortalAdmin() && !findFilter(filters, CID_FIELD).isPresent()) {
            filters.add(new FieldFilter(CID_FIELD, AND, EQUALS, cwCompanyService.getConnectWiseCid(getCurrentUser())));
        }
        if (!findFilter(filters, BOARD_ID_FIELD).isPresent()) {
            filters.add(newBoardFilter(getAccessibleBoardIds()));
        }

        try {
            URI url = urlWithConditions(ticketsUrl, filters, pageRequest);
            return pageOf(restTemplate.getForObject(url, ConnectWiseTicket[].class));
        } catch (RestClientResponseException ex) {
            log.error("Cannot load tickets: " + ex.getMessage() + "; Response body:\n" + ex.getResponseBodyAsString());
            return emptyPage();
        }
    }

    public List<ConnectWiseTicket> getOpenTickets(Set<String> cids) {
        HashSet<FieldFilter> filters = newHashSet(
                new FieldFilter(CID_FIELD, AND, IN, cids.stream().collect(joining(","))),
                new FieldFilter(STATUS_NAME_FIELD, AND, IN, TICKET_OPEN_STATUSES_STRING),
                newBoardFilter(getAccessibleBoardIds())
        );
        URI url = urlWithConditions(ticketsUrl, filters, new PageRequest(0, MAX_PAGE_SIZE));
        return asList(restTemplate.getForObject(url, ConnectWiseTicket[].class));
    }

    public TicketDetails getTicketDetails(long id) {
        CompletableFuture<ConnectWiseTicket> ticketFuture = supplyAsync(() -> getTicket(id));
        CompletableFuture<List<ConnectWiseTicketNote>> notesFuture = supplyAsync(() -> getNotes(id));
        CompletableFuture<List<ConnectWiseTimeEntry>> timeEntriesFuture = supplyAsync(() -> getTimeEntries(id));
        CompletableFuture<List<ConnectWiseScheduleEntry>> scheduleEntriesFuture = supplyAsync(() -> getScheduleEntries(id));
        CompletableFuture<List<ConnectWiseDocument>> docsFuture = supplyAsync(() -> getDocuments(id));

        ConnectWiseTicket ticket;
        List<ConnectWiseTicketNote> notes;
        List<ConnectWiseTimeEntry> timeEntries;
        List<ConnectWiseScheduleEntry> scheduleEntries;
        List<ConnectWiseDocument> docs;
        try {
            ticket = ticketFuture.get();
            notes = notesFuture.get();
            timeEntries = timeEntriesFuture.get();
            scheduleEntries = scheduleEntriesFuture.get();
            docs = docsFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Unable to load ticket details", e);
        }

        Stream<TicketComment> notesStream = notes.stream()
                .map(TicketComment::from);
        Stream<TicketComment> timeEntriesStream = timeEntries.stream()
                .map(TicketComment::from);

        List<TicketComment> comments = concat(notesStream, timeEntriesStream)
                .filter(comment -> auth.isPortalAdmin() || !comment.isInternal() && !comment.isResolution())
                .filter(note -> hasText(note.getText()))
                .sorted(comparing(TicketComment::getDateCreated).reversed())
                .collect(toList());

        List<TicketAttachment> attachments = docs.stream()
                .map(TicketAttachment::from)
                .collect(toList());

        List<String> ccEmails = ticket.getAutomaticEmailCc()
                .map(this::splitEmails)
                .orElse(emptyList());

        Optional<ShortUserInfo> contactUser = ticket.getContactEmailAddress()
                .flatMap(userService::getByEmail)
                .map(ShortUserInfo::from);

        Optional<ShortOrganizationInfo> ticketOrganization = organizationService.getOrganizationByCustomerId(ticket.getCompany().getIdentifier())
                .map(ShortOrganizationInfo::from);

        return TicketDetails.builder()
                .description(removeDescriptionComment(comments)
                        .map(TicketComment::getText)
                        .orElse(""))
                .ccEmails(ccEmails)
                .comments(comments)
                .attachments(attachments)
                .assignedMembers(scheduleEntries.stream()
                        .map(ConnectWiseScheduleEntry::getMember)
                        .collect(toList()))
                .portalUser(contactUser)
                .portalOrganization(ticketOrganization)
                .build();
    }

    public List<String> getCcEmails(long ticketIds) {
        return getTicket(ticketIds).getAutomaticEmailCc()
                .map(this::splitEmails)
                .orElse(emptyList());
    }

    public ConnectWiseTicket createTicket(TicketCreateRequest request) {
        String contactEmail = request.getContactEmail()
                .orElseGet(() -> getCurrentUser().getEmail());
        String customerId = request.getOrganizationId()
                .map(organizationService::getOrganization)
                .map(Organization::getCustomerId)
                .orElseGet(() -> cwCompanyService.getConnectWiseCid(getCurrentUser()));

        Set<String> ccEmails = new HashSet<>(request.getCcEmails());
        if (!ccEmails.contains(contactEmail)) {
            ccEmails.add(contactEmail);
        }

        ConnectWiseTicket newTicket = ConnectWiseTicket.builder()
                .board(new NamedReference(request.getBoardId().orElse(CUSTOMER_EXPERIENCE.getId())))
                .company(new CompanyReference(customerId))
                .contactEmailLookup(contactEmail)
                .contactEmailAddress(contactEmail)
                .summary(request.getSummary())
                .initialDescription(request.getDetailDescription()
                        .map(text -> embedAuthor(getCurrentUser(), text))
                        .orElse(null))
                .status(request.getStatus().map(status -> new StatusReference(status.getId())).orElse(null))
                .severity(request.getSeverity())
                .impact(request.getImpact())
                .type(new TypeReference(request.getType().getId()))
                .automaticEmailContactFlag(true)
                .automaticEmailResourceFlag(true)
                .automaticEmailCcFlag(true)
                .automaticEmailCc(joinEmails(ccEmails))
                .priority(request.getPriority())
                .build();
        return restTemplate.postForObject(ticketsUrl, newTicket, ConnectWiseTicket.class);
    }

    public ConnectWiseTicket updateTicket(long id, TicketUpdateRequest request) {
        if (request.getStatusName().isPresent() && !auth.isPortalAdmin()) {
            throw new IllegalArgumentException("Ticket status can be change by a PortalAdmin only");
        }

        List<ConnectWiseUpdateOperation> ops = new ArrayList<>();
        request.getSummary().ifPresent(summary -> ops.add(replaceOp("summary", summary)));
        request.getSeverity().ifPresent(severity -> ops.add(replaceOp("severity", severity)));
        request.getImpact().ifPresent(impact -> ops.add(replaceOp("impact", impact)));
        request.getBoardId().ifPresent(boardId -> ops.add(replaceOp("board", new IdReference(boardId))));

        if (request.getStatusName().isPresent() || request.getType().isPresent()) {
            request.getStatusName().ifPresent(status -> ops.add(replaceOp("status", new IdReference(status.getId()))));
            request.getType().ifPresent(type -> ops.add(replaceOp("type", new IdReference(type.getId()))));
        }
        request.getStatusName().ifPresent(status -> ops.add(replaceOp("status", new NamedReference(status.getId()))));
        request.getCcEmails()
                .map(this::joinEmails)
                .ifPresent(ccEmailsStr -> ops.add(replaceOp("automaticEmailCc", ccEmailsStr)));

        request.getContactEmail().ifPresent(email -> {
            Optional<ConnectWiseContact> contactOpt = userService.getByEmail(email)
                    .flatMap(cwCompanyService::getContact);
            if (contactOpt.isPresent()) {
                ConnectWiseContact contact = contactOpt.get();
                ops.add(replaceOp("contact", new IdReference(contact.getId())));
                ops.add(replaceOp("contactEmailAddress", contact.getEmail()));
                ops.add(replaceOp("contactPhoneNumber", contact.getPhone()));
                if (!request.getOrganizationId().isPresent()) {
                    ops.add(replaceOp("company", contact.getCompany()));
                }
            } else {
                ops.add(removeOp("contact"));
                ops.add(removeOp("contactPhoneNumber"));
                ops.add(replaceOp("contactName", " ")); // using " " is the only way to clear contactName
                ops.add(replaceOp("contactEmailAddress", request.getContactEmail()));
            }
        });

        request.getOrganizationId().ifPresent(orgId -> {
            Organization organization = organizationService.getOrganization(orgId);
            ops.add(replaceOp("company", new CompanyReference(organization.getCustomerId())));
        });
        request.getPriority().ifPresent(priority -> ops.add(replaceOp("priority", priority)));

        if (request.getAssignedMemberIdentifiers().isPresent()) {
            assignMembers(id, request.getAssignedMemberIdentifiers().get());
        }

        return ops.isEmpty()
                ? getTicket(id)
                : restTemplate.exchange(ticketUrl, PATCH, new HttpEntity<>(ops), ConnectWiseTicket.class, id).getBody();
    }

    private void assignMembers(long ticketId, Set<String> memberIdentifiers) {
        if (!auth.isPortalAdmin()) {
            throw new IllegalArgumentException("Only a PortalAdmin can assign members to a ticket");
        }
        List<ConnectWiseScheduleEntry> scheduleEntries = getScheduleEntries(ticketId);
        Set<String> existingMembers = scheduleEntries.stream()
                .map(entry -> entry.getMember().getIdentifier())
                .collect(toSet());

        if (existingMembers.equals(memberIdentifiers)) {
            return;
        }

        Set<String> newMembers = difference(memberIdentifiers, existingMembers);
        Set<String> removedMembers = difference(existingMembers, memberIdentifiers);

        Stream<CompletableFuture<Void>> assignmentFutures = newMembers.stream()
                .map(identifier -> runAsync(() -> createScheduleEntry(ticketId, identifier)));
        Stream<CompletableFuture<Void>> removalFutures = scheduleEntries.stream()
                .filter(entry -> removedMembers.contains(entry.getMember().getIdentifier()))
                .map(entry -> runAsync(() -> deleteScheduleEntry(entry.getId())));

        CompletableFuture[] futures = Stream.concat(assignmentFutures, removalFutures).toArray(CompletableFuture[]::new);
        allOf(futures).join();
    }

    public TicketComment createTimeEntryWithAuthor(long ticketId, TicketCommentCreateUpdateRequest comment) {
        User user = getCurrentUser();
        TicketCommentCreateUpdateRequestBuilder commentBuilder = TicketCommentCreateUpdateRequest.baseOn(comment);
        commentBuilder.text(embedAuthor(user, comment.getText()));

        if (!auth.isPortalAdmin()) {
            commentBuilder
                    .discussion(true)
                    .internal(false)
                    .resolution(false);
        }
        TicketCommentCreateUpdateRequest commentWithAuthor = commentBuilder.build();

        ConnectWiseTicket ticket = getTicket(ticketId);
        String automaticEmailCc = ticket.getAutomaticEmailCc().orElse("");
        if (!automaticEmailCc.contains(user.getEmail())) {
            List<String> emailCcs = new ArrayList<>(splitEmails(automaticEmailCc));
            emailCcs.add(user.getEmail());

            CompletableFuture<Void> updateCCFuture = runAsync(() -> updateTicketCcEmail(ticketId, String.join(EMAIL_DELIMITER, emailCcs)));
            CompletableFuture<TicketComment> createTimeEntryFuture = supplyAsync(() -> createTimeEntry(ticket, commentWithAuthor));

            try {
                updateCCFuture.get();
                return createTimeEntryFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException("Unable to create time entry", e);
            }
        } else {
            return createTimeEntry(ticket, commentWithAuthor);
        }
    }

    public TicketComment createTimeEntry(long ticketId, TicketCommentCreateUpdateRequest comment) {
        return createTimeEntry(getTicket(ticketId), comment);
    }

    private ConnectWiseScheduleEntry createScheduleEntry(long ticketId, String memberIdentifier) {
        ConnectWiseScheduleEntry scheduleEntry = ConnectWiseScheduleEntry.builder()
                .objectId(ticketId)
                .member(new MemberReference(memberIdentifier))
                .build();
        return restTemplate.postForObject(scheduleEntriesUrl, scheduleEntry, ConnectWiseScheduleEntry.class);
    }

    private void deleteScheduleEntry(long scheduleEntryId) {
        restTemplate.delete(scheduleEntryUrl, scheduleEntryId);
    }

    public byte[] downloadDocument(long documentId) {
        return restTemplate.getForObject(documentDownloadUrl, byte[].class, documentId);
    }

    public void uploadDocument(long ticketId, MultipartFile file) {
        MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
        multipartBody.add("RecordType", "Ticket");
        multipartBody.add("RecordId", String.valueOf(ticketId));
        multipartBody.add("file", asByteArrayRequest(file));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MULTIPART_FORM_DATA);

        restTemplate.postForEntity(documentsUrl, new HttpEntity<>(multipartBody, headers), String.class);
    }

    private List<ConnectWiseTicketNote> getNotes(long ticketId) {
        return asList(restTemplate.getForObject(urlWithPageSize(ticketNotesUrl, MAX_PAGE_SIZE), ConnectWiseTicketNote[].class, ticketId));
    }

    private List<ConnectWiseTimeEntry> getTimeEntries(long ticketId) {
        URI url = urlWithConditions(timeEntriesUrl, new FieldFilter("chargeToId", AND, EQUALS, String.valueOf(ticketId)));
        return asList(restTemplate.getForObject(urlWithPageSize(url.toString(), MAX_PAGE_SIZE), ConnectWiseTimeEntry[].class));
    }

    private List<ConnectWiseScheduleEntry> getScheduleEntries(long ticketId) {
        URI url = urlWithConditions(scheduleEntriesUrl, new FieldFilter("objectId", AND, EQUALS, String.valueOf(ticketId)));
        return asList(restTemplate.getForObject(urlWithPageSize(url.toString(), MAX_PAGE_SIZE), ConnectWiseScheduleEntry[].class));
    }

    private List<ConnectWiseDocument> getDocuments(long ticketId) {
        return asList(restTemplate.getForObject(urlWithPageSize(ticketDocumentsUrl, MAX_PAGE_SIZE), ConnectWiseDocument[].class, ticketId));
    }

    private TicketComment createTimeEntry(ConnectWiseTicket ticket, TicketCommentCreateUpdateRequest comment) {
        boolean internalOnly = (comment.getInternal() || comment.getResolution()) && !comment.getDiscussion();
        ConnectWiseTimeEntry timeEntry = ConnectWiseTimeEntry.builder()
                .chargeToId(ticket.getId())
                .notes(comment.getText())
                .timeStart(now().truncatedTo(SECONDS).format(ISO_INSTANT))
                .addToDetailDescriptionFlag(comment.getDiscussion())
                .addToInternalAnalysisFlag(comment.getInternal())
                .addToResolutionFlag(comment.getResolution())
                .emailContactFlag(!internalOnly && ticket.isAutomaticEmailContactFlag())
                .emailResourceFlag(!internalOnly && hasText(ticket.getResources()) && ticket.isAutomaticEmailResourceFlag())
                .emailCcFlag(!internalOnly && ticket.isAutomaticEmailCcFlag())
                .emailCc(ticket.getAutomaticEmailCc().orElse(""))
                .build();
        return TicketComment.from(restTemplate.postForObject(timeEntriesUrl, timeEntry, ConnectWiseTimeEntry.class));
    }

    private void updateTicketCcEmail(long ticketId, String ccEmails) {
        restTemplate.exchange(ticketUrl, PATCH, new HttpEntity<>(singletonList(replaceOp("automaticEmailCc", ccEmails))),
                ConnectWiseTicket.class, ticketId);
    }

    private HttpEntity<ByteArrayResource> asByteArrayRequest(MultipartFile file) {
        try {
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(parseMediaType(file.getContentType()));
            return new HttpEntity<>(fileResource, headers);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Optional<TicketComment> removeDescriptionComment(List<TicketComment> comments) {
        Iterator<TicketComment> iterator = reverse(comments).iterator();
        while (iterator.hasNext()) {
            TicketComment comment = iterator.next();
            if (comment.isDiscussion()) {
                iterator.remove();
                return Optional.of(comment);
            }
        }
        return Optional.empty();
    }

    private void checkUnsupportedFilters(Set<FieldFilter> filters) {
        filters.stream()
                .filter(filter -> !SUPPORTED_FILTERS.contains(filter.getKey()))
                .findAny()
                .ifPresent(filter -> {
                    throw new IllegalArgumentException("Unsupported filter=" + filter.getKey());
                });
        findFilter(filters, BOARD_ID_FIELD)
                .flatMap(boardFilter -> stream(boardFilter.getValue().split(","))
                        .map(Long::valueOf)
                        .filter(boardId -> !getAccessibleBoardIds().contains(boardId))
                        .findAny())
                .ifPresent(boardId -> {
                    throw new IllegalArgumentException("Unsupported boardId=" + boardId);
                });
    }

    private Set<FieldFilter> preprocessFilters(Set<FieldFilter> filters) {
        filters = new HashSet<>(filters);
        return filters.stream()
                .map(filter -> {
                    switch (filter.getKey()) {
                        case ORGANIZATION_ID_FIELD:
                            long[] ids = stream(filter.getValue().split(","))
                                    .mapToLong(Long::valueOf)
                                    .toArray();
                            String cids = organizationService.getOrganizations(ids).stream()
                                    .map(Organization::getCustomerId)
                                    .collect(joining(","));
                            return FieldFilter.baseOn(filter).key(CID_FIELD).value(cids).build();
                        case STATUS_FIELD:
                            String statusNames = stream(filter.getValue().split(","))
                                    .map(status -> Status.valueOf(status).toString())
                                    .collect(joining(","));
                            return FieldFilter.baseOn(filter).key(STATUS_NAME_FIELD).value(statusNames).build();
                        case TYPE_NAME_FIELD:
                            String typeNames = stream(filter.getValue().split(","))
                                    .map(status -> Type.valueOf(status).toString())
                                    .collect(joining(","));
                            return FieldFilter.baseOn(filter).key(TYPE_NAME_FIELD).value(typeNames).build();
                        case SEVERITY_FIELD:
                            Severity severity = Severity.valueOf(filter.getValue());
                            return FieldFilter.baseOn(filter).value(severity.toString()).build();
                        case ENTERED_DATE_FROM_FIELD:
                        case ENTERED_DATE_TO_FIELD:
                            return FieldFilter.baseOn(filter)
                                    .key("dateEntered")
                                    .value(formatTime(filter.getValue()))
                                    .build();
                        case LAST_UPDATED_FROM_FIELD:
                        case LAST_UPDATED_TO_FIELD:
                            return FieldFilter.baseOn(filter)
                                    .key("lastUpdated")
                                    .value(formatTime(filter.getValue()))
                                    .build();
                        default:
                            return filter;
                    }
                })
                .collect(toSet());
    }

    private FieldFilter newBoardFilter(Set<Long> boardIds) {
        return new FieldFilter(BOARD_ID_FIELD, AND, IN, boardIds.stream()
                .map(String::valueOf)
                .collect(joining(",")));
    }

    private Set<Long> getAccessibleBoardIds() {
        return stream(Board.values())
                .filter(board -> auth.isPortalAdmin()
                        ? board != NO_ACTION_REQUIRED
                        : board == CUSTOMER_EXPERIENCE || board == CUSTOMER_PORTAL)
                .map(Board::getId)
                .collect(toSet());
    }

    private User getCurrentUser() {
        return auth.getAuthenticatedUser().getUser();
    }

    private String formatTime(String timeInMilli) {
        String formattedTime = Instant
                .ofEpochMilli(Long.valueOf(timeInMilli))
                .atZone(ZoneId.of("UTC"))
                .truncatedTo(SECONDS)
                .format(ISO_INSTANT);

        return "[" + formattedTime + "]";
    }

    private List<String> splitEmails(String emailString) {
        return asList(StringUtils.split(emailString, EMAIL_DELIMITER));
    }

    private String joinEmails(Set<String> ccEmails) {
        return String.join(EMAIL_DELIMITER, ccEmails);
    }

}
