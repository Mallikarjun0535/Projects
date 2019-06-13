package com.dizzion.portal.domain.maintenance.persistence.entity;

import com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent.ApprovalStatus;
import com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent.Severity;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.scope.persistence.TenantResource;
import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import lombok.*;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

import static com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent.ApprovalStatus.*;
import static java.util.stream.Collectors.toSet;
import static javax.persistence.EnumType.STRING;

@Entity
@Table(name = "maintenance_event")
@TenantResource(readerTenantPath = "organizations.tenantPath")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MaintenanceEventEntity {
    @Id
    @GeneratedValue
    private long id;
    @Enumerated(STRING)
    private Type type;
    private String title;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String message;
    private ZonedDateTime startDateTime;
    private ZonedDateTime endDateTime;
    private String timezoneOffset;
    @ManyToMany
    @JoinTable(name = "maintenance_event_organization",
            inverseJoinColumns = @JoinColumn(name = "organization_id"),
            joinColumns = @JoinColumn(name = "maintenance_event_id"))
    private Set<OrganizationEntity> organizations;
    @ManyToOne
    private OrganizationEntity owner;
    private boolean reminder;
    private ZonedDateTime reminderSentAt;
    @Singular
    @OneToMany(mappedBy = "maintenanceEvent")
    private Set<MaintenanceEventApprovalEntity> approvals;
    private Long approvalTicketId;
    private String cron;
    private LocalDate repeatUntil;
    @Enumerated(STRING)
    private Severity severity;
    @ManyToOne
    private UserEntity assignedUser;

    public Optional<Long> getApprovalTicketId() {
        return Optional.ofNullable(approvalTicketId);
    }

    public Optional<String> getCron() {
        return Optional.ofNullable(cron);
    }

    public Optional<LocalDate> getRepeatUntil() {
        return Optional.ofNullable(repeatUntil);
    }

    public Optional<UserEntity> getAssignedUser() {
        return Optional.ofNullable(assignedUser);
    }

    public boolean isRecurring() {
        return getCron().isPresent();
    }

    public Duration getDuration() {
        return Duration.between(getStartDateTime(), getEndDateTime());
    }

    public boolean occursBetween(ZonedDateTime fromInclusive, ZonedDateTime untilExclusive) {
        return startDateTime.isBefore(untilExclusive) && (endDateTime.isAfter(fromInclusive) || endDateTime.isEqual(fromInclusive));

    }

    public ApprovalStatus getApprovalStatus() {
        boolean everybodyResponded = approvals.stream()
                .map(approval -> approval.getUser().getOrganization())
                .collect(toSet())
                .containsAll(organizations);
        boolean hasRejections = approvals.stream().anyMatch(approval -> !approval.isApproved());

        if (hasRejections) {
            return REJECTED;
        } else if (everybodyResponded) {
            return APPROVED;
        } else {
            return PENDING;
        }
    }

    public enum Type {
        MAINTENANCE,
        DIFFERENTIAL_BACKUP,
        FULL_BACKUP,
        DESKTOP_PATCHING,
        INFRASTRUCTURE_PATCHING;

        public String getHumanReadableName() {
            return toString().toLowerCase().replace('_', ' ');
        }
    }
}
