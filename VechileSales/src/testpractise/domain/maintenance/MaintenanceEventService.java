package com.dizzion.portal.domain.maintenance;

import com.dizzion.portal.domain.attachment.AttachmentService;
import com.dizzion.portal.domain.common.AbstractCrudService;
import com.dizzion.portal.domain.common.ScopedEntityService;
import com.dizzion.portal.domain.exception.EntityNotFoundException;
import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.helpdesk.dto.TicketAttachment;
import com.dizzion.portal.domain.helpdesk.dto.TicketComment;
import com.dizzion.portal.domain.helpdesk.dto.TicketCommentCreateUpdateRequest;
import com.dizzion.portal.domain.maintenance.dto.*;
import com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent.ApprovalStatus;
import com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent.ProgressStatus;
import com.dizzion.portal.domain.maintenance.persistence.MaintenanceEventApprovalRepository;
import com.dizzion.portal.domain.maintenance.persistence.MaintenanceEventInstanceRepository;
import com.dizzion.portal.domain.maintenance.persistence.MaintenanceEventRepository;
import com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventApprovalEntity;
import com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventEntity;
import com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventInstanceEntity;
import com.dizzion.portal.domain.organization.dto.Organization;
import com.dizzion.portal.domain.organization.persistence.OrganizationRepository;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.user.dto.ShortUserInfo;
import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

import static com.dizzion.portal.domain.common.DateUtils.streamDates;
import static com.dizzion.portal.domain.helpdesk.dto.TicketCommentCreateUpdateRequest.discussionComment;
import static com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent.ApprovalStatus.APPROVED;
import static com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent.ProgressStatus.NOT_STARTED;
import static com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent.ProgressStatus.STARTED;
import static com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent.Severity.LOW;
import static com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventEntity.Type.MAINTENANCE;
import static com.dizzion.portal.domain.scope.TenantPathUtils.isChildTenantPath;
import static com.dizzion.portal.domain.scope.TenantPathUtils.tenantScope;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.time.ZonedDateTime.now;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

@Service
@Transactional
@Slf4j
public class MaintenanceEventService extends AbstractCrudService<MaintenanceEventEntity> {

    private final AuthenticatedUserAccessor auth;
    private final MaintenanceEventRepository eventRepo;
    private final MaintenanceEventApprovalRepository approvalRepo;
    private final AttachmentService attachmentService;
    private final String bucketName;
    private final OrganizationRepository organizationRepository;
    private final MaintenanceEventInstanceRepository maintenanceInstanceRepo;
    private final MaintenanceEventEmailService maintenanceEventEmailService;
    private final MaintenanceTicketService maintenanceTicketService;

    public MaintenanceEventService(ScopedEntityService entityService,
                                   AuthenticatedUserAccessor auth,
                                   MaintenanceEventRepository eventRepo,
                                   AttachmentService attachmentService,
                                   @Value("${aws.maintenance-events.s3-bucket-name}") String bucketName,
                                   OrganizationRepository organizationRepository,
                                   MaintenanceEventApprovalRepository approvalRepo,
                                   MaintenanceEventInstanceRepository maintenanceInstanceRepo,
                                   MaintenanceEventEmailService maintenanceEventEmailService,
                                   MaintenanceTicketService maintenanceTicketService) {
        super(entityService);
        this.eventRepo = eventRepo;
        this.auth = auth;
        this.approvalRepo = approvalRepo;
        this.attachmentService = attachmentService;
        this.bucketName = bucketName;
        this.maintenanceInstanceRepo = maintenanceInstanceRepo;
        this.maintenanceTicketService = maintenanceTicketService;
        this.organizationRepository = organizationRepository;
        this.maintenanceEventEmailService = maintenanceEventEmailService;
    }

    @Transactional(readOnly = true)
    public Page<MaintenanceEvent> getPage(Pageable pageRequest, Set<FieldFilter> filters) {
        System.out.println("+++++22222222222222222222");
        return getEntitiesPage(pageRequest, filters).map(eventEntity -> MaintenanceEvent.baseOn(eventEntity)
                .approvals(getApprovals(eventEntity))
                .build());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceEvent> getAllScheduledJobsInstancesBetween(LocalDate from, LocalDate until) {
        return eventRepo.findInDateRange(from, until, tenantScope(auth.getTenantPath())).stream()
                .flatMap(eventEntity -> streamInstancesBetween(eventEntity, from, until))
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceEvent> getMaintenanceEventInstancesBetween(LocalDate from, LocalDate until) {
        return eventRepo.findInDateRange(from, until, tenantScope(auth.getTenantPath())).stream()
                .filter(eventEntity -> eventEntity.getType() == MAINTENANCE)
                .flatMap(eventEntity -> streamInstancesBetween(eventEntity, from, until))
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public MaintenanceEvent getMaintenanceEvent(long id) {
        return getMaintenanceEventInstance(id, getForRead(id).getStartDateTime().toLocalDate());
    }

    @Transactional(readOnly = true)
    public MaintenanceEvent getMaintenanceEventInstance(long id, LocalDate instanceStartDate) {
        return getMaintenanceEventInstance(getForRead(id), instanceStartDate);
    }

    @Transactional(readOnly = true)
    public MaintenanceEvent getMaintenanceEventInstanceForCron(MaintenanceEventEntity eventEntity, LocalDate instanceStartDate) {
        return streamInstancesBetween(eventEntity, instanceStartDate, instanceStartDate)
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public MaintenanceEvent getMaintenanceEventInstance(MaintenanceEventEntity eventEntity, LocalDate instanceStartDate) {
        return streamInstancesBetween(eventEntity, instanceStartDate, instanceStartDate)
                .findFirst()
                .orElseThrow(EntityNotFoundException::new);
    }

    public MaintenanceEvent preCreate() {
        OrganizationEntity owner = organizationRepository.findOne(auth.getOrganization().getId());

        MaintenanceEventEntity entity = save(MaintenanceEventEntity.builder()
                .type(MAINTENANCE)
                .title("New Scheduled Job")
                .message("")
                .startDateTime(now())
                .endDateTime(now())
                .timezoneOffset(ZoneOffset.systemDefault().getId())
                .organizations(emptySet())
                .severity(LOW)
                .owner(owner)
                .build());
        return MaintenanceEvent.from(entity);
    }

    public MaintenanceEvent update(long id, MaintenanceEventUpdateRequest request, Optional<MultipartFile[]> cref) {
        Set<OrganizationEntity> organizations = scopedEntityService
                .getForWrite(request.getOrganizationIds(), OrganizationEntity.class);
        MaintenanceEventEntity existing = getForWrite(id);

        Set<OrganizationEntity> oldOrgs = existing.getOrganizations();
        Set<Organization> newOrgs = organizations.stream()
                .filter(org -> !oldOrgs.contains(org))
                .map(Organization::from)
                .collect(toSet());

        existing.setType(request.getType());
        existing.setTitle(request.getTitle());
        existing.setMessage(request.getMessage());
        existing.setStartDateTime(request.getStartDateTime());
        existing.setEndDateTime(request.getEndDateTime());
        existing.setOrganizations(organizations);
        existing.setReminder(request.getReminder());
        existing.setCron(request.getCron().orElse(null));
        existing.setTimezoneOffset(request.getTimezoneOffset());
        existing.setRepeatUntil(existing.isRecurring()
                ? request.getRepeatUntil()
                : null);
        existing.setSeverity(request.getSeverity());

        request.getAssignedUserId()
                .flatMap(userId -> Optional.ofNullable(getUserEntity(userId)))
                .ifPresent(existing::setAssignedUser);

        if (oldOrgs.isEmpty() && auth.isPortalAdmin()) {

           long ticketId = cref
                    .map(file -> maintenanceTicketService.createApprovalTicket(existing, file, request.getRecurrencePatternName()))
                    .orElseGet(() -> maintenanceTicketService.createApprovalTicket(existing, request.getRecurrencePatternName()));
         /*   if( cref.isPresent() && ArrayUtils.isNotEmpty(cref.get())){
                ticketId =  maintenanceTicketService.createApprovalTicket(existing, cref.get(), request.getRecurrencePatternName());
            }else{
                ticketId = maintenanceTicketService.createApprovalTicket(existing, request.getRecurrencePatternName());
            }*/
            existing.setApprovalTicketId(ticketId);
        }

        MaintenanceEvent maintenance = MaintenanceEvent.from(save(existing));
        maintenanceEventEmailService.sendApprovalRequests(maintenance, newOrgs, request.getRecurrencePatternName());
        return maintenance;
    }

    public String uploadAttachment(long maintenanceEventId, MultipartFile file) {
        checkAccess(maintenanceEventId);
        return attachmentService.uploadPublicAttachment(bucketName, maintenanceEventId, file);
    }

    @Transactional(readOnly = true)
    public List<TicketComment> getComments(long maintenanceEventId, LocalDate instanceStartDate) {
        MaintenanceEvent maintenanceEvent = getMaintenanceEventInstance(maintenanceEventId, instanceStartDate);
        return maintenanceTicketService.getProgressTicketComments(maintenanceEvent);
    }

    @Transactional(readOnly = true)
    public void addComment(long maintenanceEventId,
                           LocalDate instanceStartDate,
                           TicketCommentCreateUpdateRequest comment,
                           Optional<MultipartFile> attachment) {
        MaintenanceEvent maintenanceEvent = getMaintenanceEventInstance(maintenanceEventId, instanceStartDate);
        maintenanceTicketService.commentProgressTickets(maintenanceEvent, comment, attachment);
    }

    @Transactional(readOnly = true)
    public List<TicketAttachment> getProgressTicketAttachments(long maintenanceEventId, LocalDate instanceStartDate) {
        MaintenanceEvent maintenanceEvent = getMaintenanceEventInstance(maintenanceEventId, instanceStartDate);
        return maintenanceTicketService.getProgressTicketAttachments(maintenanceEvent);
    }

    @Override
    public void delete(long id) {
        MaintenanceEventEntity event = getForWrite(id);
        maintenanceTicketService.closeApprovalTicket(event, "Maintenance window deleted from Portal");
        attachmentService.deletePublicAttachment(bucketName, id);
        super.delete(id);
    }

    public void delete(long id, LocalDate instanceStartDate) {
        MaintenanceEventEntity eventEntity = getForWrite(id);
        if (!eventEntity.isRecurring()) {
            delete(id);
        } else {
            materializeMaintenanceInstance(eventEntity, instanceStartDate).setRemoved(true);
        }
    }

    public MaintenanceEvent approve(long id) {
        MaintenanceEventEntity eventEntity = getForRead(id);
        ApprovalStatus previousStatus = eventEntity.getApprovalStatus();

        if (isCurrentUserHasApproval(eventEntity, true)) {
            return MaintenanceEvent.from(eventEntity);
        }

        eventEntity.getApprovals().add(saveApproval(eventEntity, true)); //collections are not reloaded automatically
        maintenanceTicketService.commentApprovalTicket(eventEntity,
                discussionComment("Approved by " + getCurrentUser().toNameEmailOrganizationString()));

        MaintenanceEvent eventInstance = MaintenanceEvent.from(eventEntity);
        ApprovalStatus newStatus = eventEntity.getApprovalStatus();
        if (newStatus != previousStatus && newStatus == APPROVED) {
            auth.asAdmin(() -> maintenanceTicketService.closeApprovalTicket(eventEntity, "Approved by all affected organizations"));
            maintenanceEventEmailService.sendNotifications(eventInstance);
        }
        return eventInstance;
    }

    public MaintenanceEvent reject(long id) {
        MaintenanceEventEntity eventEntity = getForRead(id);

        if (isCurrentUserHasApproval(eventEntity, false)) {
            return MaintenanceEvent.from(eventEntity);
        }
        if (eventEntity.getApprovalStatus() == APPROVED) {
            throw new IllegalStateException("Cannot reject an approved maintenance event");
        }

        eventEntity.getApprovals().add(saveApproval(eventEntity, false)); //collections are not reloaded automatically
        maintenanceTicketService.commentApprovalTicket(eventEntity,
                discussionComment("Rejected by " + getCurrentUser().toNameEmailOrganizationString()));
        return MaintenanceEvent.from(eventEntity);
    }

    public MaintenanceEvent updateProgress(long id, MaintenanceEventProgressUpdateRequest request) {
        if (!auth.isPortalAdmin()) {
            throw new AccessDeniedException("Only portal admins are allowed to change maintenance status");
        }
        checkArgument(request.getStatus() != NOT_STARTED);

        LocalDate instanceStartDate = request.getInstanceStartDate();
        MaintenanceEventEntity eventEntity = getForWrite(id);
        MaintenanceEventInstanceEntity instanceEntity = materializeMaintenanceInstance(eventEntity, instanceStartDate);

        ProgressStatus newStatus = request.getStatus();
        ProgressStatus oldStatus = instanceEntity.getProgressStatus();
        if (oldStatus == newStatus) {
            return getMaintenanceEventInstance(id, instanceStartDate);
        }

        MaintenanceEvent eventInstance;
        switch (newStatus) {
            case STARTED:
                checkState(oldStatus == NOT_STARTED);

                instanceEntity.setProgressStatus(newStatus);
                eventInstance = getMaintenanceEventInstance(id, instanceStartDate);
                maintenanceTicketService.createProgressTickets(eventInstance);
                break;
            case FINISHED:
                checkState(oldStatus == STARTED);

                instanceEntity.setProgressStatus(newStatus);
                eventInstance = getMaintenanceEventInstance(id, instanceStartDate);
                maintenanceTicketService.finishProgressTickets(eventInstance);
                break;
            case FAILED:
                checkState(oldStatus == NOT_STARTED || oldStatus == STARTED);
                checkArgument(request.getFailureReason().isPresent());

                instanceEntity.setProgressStatus(newStatus);
                eventInstance = getMaintenanceEventInstance(id, instanceStartDate);
                maintenanceTicketService.failProgressTickets(eventInstance, request.getFailureReason().get());
                break;
            default:
                throw new IllegalStateException();
        }
        maintenanceEventEmailService.sendProgressNotifications(eventInstance);
        return eventInstance;
    }

    public MaintenanceEvent assignUser(long id, MaintenanceEventUserAssignmentRequest request) {
        MaintenanceEventEntity eventEntity = getForRead(id);
        UserEntity userEntity = getUserEntity(request.getUserId());
        LocalDate instanceStartDate = request.getInstanceStartDate();

        MaintenanceEventInstanceEntity eventInstanceEntity = materializeMaintenanceInstance(eventEntity, instanceStartDate);
        eventInstanceEntity.setAssignedUser(userEntity);

        MaintenanceEvent eventInstance = getMaintenanceEventInstance(eventEntity, instanceStartDate);
        maintenanceTicketService.assignUserToTickets(eventInstance, userEntity.getEmail());
        return eventInstance;
    }

    private MaintenanceEventInstanceEntity materializeMaintenanceInstance(MaintenanceEventEntity eventEntity, LocalDate instanceStartDate) {
        ZonedDateTime startDateTime = sameTimeOnAnotherDate(eventEntity.getStartDateTime(), instanceStartDate);
        return maintenanceInstanceRepo.findByMaintenanceEventIdAndStartDateTime(eventEntity.getId(), startDateTime)
                .orElseGet(() -> maintenanceInstanceRepo.save(MaintenanceEventInstanceEntity.builder()
                        .maintenanceEvent(eventEntity)
                        .startDateTime(startDateTime)
                        .progressStatus(NOT_STARTED)
                        .build()));
    }

    private boolean isCurrentUserHasApproval(MaintenanceEventEntity eventEntity, boolean approvalStatus) {
        return eventEntity.getApprovals()
                .stream()
                .anyMatch(approval -> (approval.getUser().getId() == getCurrentUser().getId())
                        && (approval.isApproved() == approvalStatus));
    }

    private Stream<MaintenanceEvent> streamInstancesBetween(MaintenanceEventEntity eventEntity, LocalDate from, LocalDate until) {
        List<ZonedDateTime> instanceStartDateTimes = getInstanceStartDateTimes(eventEntity, from, until);

        Set<MaintenanceEventApproval> approvals = getApprovals(eventEntity);
        Map<ZonedDateTime, MaintenanceEventInstanceEntity> materializedInstancesByDate = maintenanceInstanceRepo
                .findByMaintenanceEventIdAndStartDateTimeIn(eventEntity.getId(), instanceStartDateTimes).stream()
                .collect(toMap(
                        MaintenanceEventInstanceEntity::getStartDateTime,
                        instanceEntity -> instanceEntity));

        return instanceStartDateTimes.stream()
                .filter(dateTime -> Optional.ofNullable(materializedInstancesByDate.get(dateTime))
                        .map(instanceEntity -> !instanceEntity.isRemoved())
                        .orElse(true))
                .map(dateTime -> {
                    Optional<MaintenanceEventInstanceEntity> instanceEntityOpt = Optional.ofNullable(materializedInstancesByDate.get(dateTime));

                    ProgressStatus status = instanceEntityOpt
                            .map(MaintenanceEventInstanceEntity::getProgressStatus)
                            .orElse(NOT_STARTED);
                    Optional<UserEntity> assignedUser = instanceEntityOpt
                            .filter(instanceEntity -> instanceEntity.getAssignedUser().isPresent())
                            .map(MaintenanceEventInstanceEntity::getAssignedUser)
                            .orElseGet(eventEntity::getAssignedUser);
                    Map<Long, Long> progressTicketIdsByOrganizationId = instanceEntityOpt
                            .map(instanceEntity -> maintenanceTicketService.getProgressTicketIdsByOrganizationId(instanceEntity.getId()))
                            .orElse(emptyMap());

                    return MaintenanceEvent.baseOn(eventEntity)
                            .startDateTime(dateTime)
                            .endDateTime(dateTime.plus(eventEntity.getDuration()))
                            .progressStatus(Optional.of(status))
                            .progressTicketIdsByOrganizationId(progressTicketIdsByOrganizationId)
                            .assignedUser(assignedUser.map(ShortUserInfo::from))
                            .approvals(approvals)
                            .build();
                });
    }

    private List<ZonedDateTime> getInstanceStartDateTimes(MaintenanceEventEntity eventEntity, LocalDate from, LocalDate until) {
        ZonedDateTime fromDateTime = from.atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime untilDateTime = until.plusDays(1).atStartOfDay(ZoneId.systemDefault());
        if (!eventEntity.isRecurring()) {
            return eventEntity.occursBetween(fromDateTime, untilDateTime)
                    ? singletonList(eventEntity.getStartDateTime())
                    : emptyList();
        }
        LocalDate actualFrom = from.minusDays(eventEntity.getDuration().toDays());
        LocalDate actualUntil = eventEntity.getRepeatUntil()
                .filter(repeatUntil -> repeatUntil.isBefore(until))
                .orElse(until);

        CronTrigger cronTrigger = CronTrigger.from(eventEntity);
        return streamDates(actualFrom, actualUntil)
                .filter(date -> cronTrigger.triggersOn(date, eventEntity.getStartDateTime().toLocalTime()))
                .map(date -> sameTimeOnAnotherDate(eventEntity.getStartDateTime(), date))
                .collect(toList());
    }

    public MaintenanceEvent getNextOccurrenceInstance(long id) {
        MaintenanceEventEntity eventEntity = getForRead(id);
        LocalDate nextOccurrenceDate = CronTrigger.from(eventEntity).nextOccurrence(now())
                .orElseThrow(() -> new IllegalArgumentException("Finished job, id=" + id));
        return getMaintenanceEventInstance(id, nextOccurrenceDate);
    }

    private ZonedDateTime sameTimeOnAnotherDate(ZonedDateTime sourceDateTime, LocalDate targetDate) {
        return targetDate.atStartOfDay(sourceDateTime.getZone()).with(sourceDateTime.toLocalTime());
    }

    private MaintenanceEventApprovalEntity saveApproval(MaintenanceEventEntity event, boolean approved) {
        MaintenanceEventApprovalEntity approval = approvalRepo
                .findByMaintenanceEventIdAndUserId(event.getId(), getCurrentUser().getId())
                .orElseGet(() -> approvalRepo.save(MaintenanceEventApprovalEntity.builder()
                        .maintenanceEvent(event)
                        .user(getUserEntity(getCurrentUser().getId()))
                        .approved(approved)
                        .build()));
        approval.setApproved(approved);
        return approval;
    }

    private Set<MaintenanceEventApproval> getApprovals(MaintenanceEventEntity event) {
        return event.getApprovals().stream()
                .filter(approval -> isChildTenantPath(approval.getUser().getOrganization().getTenantPath(), auth.getOrganization().getTenantPath()))
                .map(approval -> {
                    ShortUserInfo userInfo = ShortUserInfo.from(approval.getUser());
                    return new MaintenanceEventApproval(userInfo, approval.isApproved());
                })
                .collect(toSet());
    }

    private User getCurrentUser() {
        return auth.getAuthenticatedUser().getUser();
    }

    private UserEntity getUserEntity(Long userId) {
        return scopedEntityService.getForRead(userId, UserEntity.class);
    }
}
