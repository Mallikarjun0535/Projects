package com.dizzion.portal.domain.maintenance.dto;

import com.dizzion.portal.domain.common.dto.ShortEntityInfo;
import com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventApprovalEntity;
import com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventEntity;
import com.dizzion.portal.domain.maintenance.persistence.entity.MaintenanceEventEntity.Type;
import com.dizzion.portal.domain.organization.dto.Organization;
import com.dizzion.portal.domain.user.dto.ShortUserInfo;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

@Value
@Builder
public class MaintenanceEvent {
    Long id;
    Type type;
    String title;
    String message;
    ZonedDateTime startDateTime;
    ZonedDateTime endDateTime;
    Set<Organization> organizations;
    String ownerTenantPath;
    ShortEntityInfo owner;
    boolean reminder;
    ApprovalStatus approvalStatus;
    Set<MaintenanceEventApproval> approvals;
    Optional<Long> approvalTicketId;
    Optional<String> cron;
    Optional<LocalDate> repeatUntil;
    boolean recurring;
    String timezoneOffset;
    ZonedDateTime firstInstanceStartDateTime;
    Optional<ProgressStatus> progressStatus;
    Map<Long, Long> progressTicketIdsByOrganizationId;
    Severity severity;
    Optional<ShortUserInfo> assignedUser;

    public static MaintenanceEventBuilder baseOn(MaintenanceEventEntity entity) {
        return MaintenanceEvent.builder()
                .id(entity.getId())
                .type(entity.getType())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .startDateTime(entity.getStartDateTime())
                .endDateTime(entity.getEndDateTime())
                .organizations(entity.getOrganizations().stream().map(Organization::from).collect(toSet()))
                .ownerTenantPath(entity.getOwner().getTenantPath())
                .owner(new ShortEntityInfo(entity.getOwner().getId(), entity.getOwner().getName()))
                .approvalStatus(entity.getApprovalStatus())
                .approvals(emptySet())
                .reminder(entity.isReminder())
                .approvalTicketId(entity.getApprovalTicketId())
                .repeatUntil(entity.getRepeatUntil())
                .cron(entity.getCron())
                .severity(entity.getSeverity())
                .recurring(entity.isRecurring())
                .timezoneOffset(entity.getTimezoneOffset())
                .firstInstanceStartDateTime(entity.getStartDateTime())
                .assignedUser(entity.getAssignedUser().map(ShortUserInfo::from));
    }

    public static MaintenanceEvent from(MaintenanceEventEntity entity) {
        return baseOn(entity).build();
    }

    public enum ApprovalStatus {
        PENDING,
        APPROVED,
        REJECTED;

        public static ApprovalStatus from(MaintenanceEventApprovalEntity approval) {
            return Optional.ofNullable(approval)
                    .map(MaintenanceEventApprovalEntity::isApproved)
                    .map(approved -> approved ? APPROVED : REJECTED)
                    .orElse(PENDING);
        }
    }

    public enum ProgressStatus {
        NOT_STARTED,
        STARTED,
        FINISHED,
        FAILED
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH
    }
}
