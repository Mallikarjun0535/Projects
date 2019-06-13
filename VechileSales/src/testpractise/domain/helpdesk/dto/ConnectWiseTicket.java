package com.dizzion.portal.domain.helpdesk.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

import static com.google.common.collect.Maps.uniqueIndex;
import static java.util.Arrays.asList;
import static org.springframework.util.StringUtils.capitalize;

@Data
@Builder
public class ConnectWiseTicket {
    Long id;
    NamedReference board;
    CompanyReference company;
    String contactName;
    String contactEmailLookup;
    String contactEmailAddress;
    StatusReference status;
    String summary;
    String initialDescription;
    Severity severity;
    TypeReference type;
    NamedReference priority;
    NamedReference team;
    Impact impact;
    boolean automaticEmailContactFlag;
    boolean automaticEmailCcFlag;
    boolean automaticEmailResourceFlag;
    String automaticEmailCc;
    String resources;
    ZonedDateTime dateEntered;
    Info _info;
    MemberReference owner;

    public Optional<String> getAutomaticEmailCc() {
        return Optional.ofNullable(automaticEmailCc);
    }

    public Optional<String> getContactEmailAddress() {
        return Optional.ofNullable(contactEmailAddress);
    }

    @Value
    public static class StatusReference {
        long id;
        Status name;

        @JsonCreator
        public StatusReference(long id, String name) {
            this.id = id;
            this.name = Status.byId(id);
        }

        public StatusReference(long id) {
            this.id = id;
            this.name = Status.byId(id);
        }
    }

    @Value
    public static class TypeReference {
        long id;
        Type name;

        @JsonCreator
        public TypeReference(long id, String name) {
            this.id = id;
            this.name = Type.byId(id);
        }

        public TypeReference(long id) {
            this.id = id;
            this.name = null;
        }
    }

    @Value
    private static class Info {
        ZonedDateTime lastUpdated;
    }

    @RequiredArgsConstructor
    @Getter
    public enum Board {
        CUSTOMER_EXPERIENCE("Customer Experience", 22),
        CUSTOMER_PORTAL("Customer Portal", 45),
        PROVISIONING("Provisioning", 36),
        CHANGE_MANAGEMENT("Change Management", 38),
        CLIENT_RELATIONS("Client Relations", 39),
        PLATFORM("Platform", 43),
        NO_ACTION_REQUIRED("No Action Required", 46);

        private final String name;
        private final long id;
    }

    @RequiredArgsConstructor
    @Getter
    public enum Status {
        CUSTOMER_EXPERIENCE_NEW("New", 445),
        CUSTOMER_EXPERIENCE_ASSIGNED("Assigned", 449),
        CUSTOMER_EXPERIENCE_SCHEDULED("Scheduled", 450),
        CUSTOMER_EXPERIENCE_IN_PROGRESS("In Progress", 446),
        CUSTOMER_EXPERIENCE_ON_HOLD("On hold (Provide Reason)", 534),
        CUSTOMER_EXPERIENCE_WAITING_ON_CLIENT("Waiting on Client", 447),
        CUSTOMER_EXPERIENCE_WAITING_ON_VENDOR("Waiting on Vendor", 517),
        CUSTOMER_EXPERIENCE_COMPLETED("Completed", 519),
        CUSTOMER_EXPERIENCE_CLOSED("Closed", 448),
        CUSTOMER_EXPERIENCE_CANCELLED("Cancelled", 563),
        CUSTOMER_EXPERIENCE_CLOSED_DUPLICATE_NO_NOTIFICATION("Closed-Duplicate-NoNotification", 590),
        CUSTOMER_EXPERIENCE_WAITING_ON_CLIENT_REMINDER_1_OF_2("Waiting on Client Reminder 1 of 2", 527),
        CUSTOMER_EXPERIENCE_WAITING_ON_CLIENT_REMINDER_2_OF_2("Waiting on Client Reminder 2 of 2", 528),
        CUSTOMER_EXPERIENCE_FINAL_REMINDER_OF_TICKET_CLOSURE("Final Reminder of Ticket Closure", 529),

        CUSTOMER_PORTAL_NEW("New", 648),
        CUSTOMER_PORTAL_ASSIGNED("Assigned", 652),
        CUSTOMER_PORTAL_SCHEDULED("Scheduled", 653),
        CUSTOMER_PORTAL_IN_PROGRESS("In Progress", 649),
        CUSTOMER_PORTAL_ON_HOLD("On hold (Provide Reason)", 659),
        CUSTOMER_PORTAL_WAITING_ON_CLIENT("Waiting on Client", 650),
        CUSTOMER_PORTAL_WAITING_ON_VENDOR("Waiting on Vendor", 654),
        CUSTOMER_PORTAL_COMPLETED("Completed", 655),
        CUSTOMER_PORTAL_CLOSED("Closed", 651),
        CUSTOMER_PORTAL_CANCELLED("Cancelled", 660),
        CUSTOMER_PORTAL_CLOSED_DUPLICATE_NO_NOTIFICATION("Closed-Duplicate-NoNotification", 661),
        CUSTOMER_PORTAL_WAITING_ON_CLIENT_REMINDER_1_OF_2("Waiting on Client Reminder 1 of 2", 656),
        CUSTOMER_PORTAL_WAITING_ON_CLIENT_REMINDER_2_OF_2("Waiting on Client Reminder 2 of 2", 657),
        CUSTOMER_PORTAL_FINAL_REMINDER_OF_TICKET_CLOSURE("Final Reminder of Ticket Closure", 658),

        PROVISIONING_NEW("New", 564),
        PROVISIONING_IN_PROGRESS("In Progress", 565),
        PROVISIONING_CLOSED("Closed", 567),
        PROVISIONING_ON_HOLD("On Hold", 574),

        CHANGE_MANAGEMENT_NEW("New", 591),
        CHANGE_MANAGEMENT_APPROVED("Approved", 592),
        CHANGE_MANAGEMENT_DECLINED("Declined", 593),
        CHANGE_MANAGEMENT_CLOSED("New", 594),
        CHANGE_MANAGEMENT_PENDING("Pending", 633),

        CLIENT_RELATIONS_NEW("New", 605),
        CLIENT_RELATIONS_ASSIGNED("Assigned", 606),
        CLIENT_RELATIONS_SCHEDULED("Scheduled", 607),
        CLIENT_RELATIONS_IN_PROGRESS("In Progress", 608),
        CLIENT_RELATIONS_ON_HOLD("On Hold (Provide Reason)", 609),
        CLIENT_RELATIONS_WAITING_ON_CLIENT("Waiting on Client", 610),
        CLIENT_RELATIONS_WAITING_ON_VENDOR("Waiting on Vendor", 611),
        CLIENT_RELATIONS_COMPLETED("Completed", 612),
        CLIENT_RELATIONS_CLOSED("Closed", 613),

        PLATFORM_NEW("New", 628),
        PLATFORM_ASSIGNED("Assigned", 629),
        PLATFORM_IN_PROGRESS("In Progress", 630),
        PLATFORM_CLOSED("Closed", 631),
        PLATFORM_SCHEDULED("Scheduled", 632),

        NO_ACTION_REQUIRED_CLOSED("New", 666);

        private static final Map<Long, Status> statusesById = uniqueIndex(asList(Status.values()), Status::getId);
        private final String name;
        private final long id;

        public static Status byId(long id) {
            return statusesById.get(id);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @RequiredArgsConstructor
    @Getter
    public enum Type {
        CUSTOMER_EXPERIENCE_PLATFORM_FAILURE("Platform Failure", 209),
        CUSTOMER_EXPERIENCE_MUST_CHANGE("MUST CHANGE", 210),
        CUSTOMER_EXPERIENCE_PLATFORM_FAILOVER("Platform Failover", 211),
        CUSTOMER_EXPERIENCE_BREAK_FIX("Break Fix", 212),
        CUSTOMER_EXPERIENCE_SPAM("Spam", 226),
        CUSTOMER_EXPERIENCE_GENERAL_INQUIRY("General Inquiry", 234),
        CUSTOMER_EXPERIENCE_DUPLICATE_ISSUE("Duplicate Issue", 236),
        CUSTOMER_EXPERIENCE_PROV_NEW("PROV-New", 288),
        CUSTOMER_EXPERIENCE_PROV_UPGRADE("PROV-Upgrade", 289),
        CUSTOMER_EXPERIENCE_CUSTOMER_OUTAGE("Customer Outage", 290),
        CUSTOMER_EXPERIENCE_PROV_DECOMMISSION("PROV-Decommission", 313),
        CUSTOMER_EXPERIENCE_GOLDEN_IMAGE_CHANGE("Golden Image Change", 334),
        CUSTOMER_EXPERIENCE_SERVICE_DELIVERY("Service Delivery", 335),
        CUSTOMER_EXPERIENCE_MWIN("MWin", 368),
        CUSTOMER_EXPERIENCE_DEN("DEN", 370),
        CUSTOMER_EXPERIENCE_IGEL("IGEL", 371),

        CUSTOMER_PORTAL_PLATFORM_FAILURE("Platform Failure", 355),
        CUSTOMER_PORTAL_MUST_CHANGE("MUST CHANGE", 356),
        CUSTOMER_PORTAL_PLATFORM_FAILOVER("Platform Failover", 357),
        CUSTOMER_PORTAL_BREAK_FIX("Break Fix", 358),
        CUSTOMER_PORTAL_SPAM("Spam", 359),
        CUSTOMER_PORTAL_GENERAL_INQUIRY("General Inquiry", 360),
        CUSTOMER_PORTAL_DUPLICATE_ISSUE("Duplicate Issue", 361),
        CUSTOMER_PORTAL_PROV_NEW("PROV-New", 362),
        CUSTOMER_PORTAL_PROV_UPGRADE("PROV-Upgrade", 363),
        CUSTOMER_PORTAL_CUSTOMER_OUTAGE("Customer Outage", 364),
        CUSTOMER_PORTAL_PROV_DECOMMISSION("PROV-Decommission", 365),
        CUSTOMER_PORTAL_GOLDEN_IMAGE_CHANGE("Golden Image Change", 366),
        CUSTOMER_PORTAL_SERVICE_DELIVERY("Service Delivery", 367),
        CUSTOMER_PORTAL_MWIN("MWin", 369),

        PROVISIONING_SECURITY_RELATED_INCIDENT_EVENT("Security Related Incident Event", 291),
        PROVISIONING_NON_SECURITY_INCIDENT_EVENT("Non-Security Incident Event", 292),
        PROVISIONING_PROV_NEW("PROV-New", 373),
        PROVISIONING_PROV_UPGRADE("PROV-Upgrade", 374),
        PROVISIONING_PROV_DECOMMISSION("PROV-Decommission", 375),
        PROVISIONING_IGEL("IGEL", 376),

        CHANGE_MANAGEMENT_NORMAL_CHANGE_REQUEST("Normal Change Request", 314),
        CHANGE_MANAGEMENT_PRIORITY_CHANGE_REQUEST("Priority Change Request", 315),
        CHANGE_MANAGEMENT_PREAPPROVED_CHANGE_REQUEST("PreApproved Change Request", 316),
        CHANGE_MANAGEMENT_EMERGENCY_CHANGE_REQUEST("Emergency Change Request", 317),

        CLIENT_RELATIONS_REPORT_REQUEST("Report Request", 321),
        CLIENT_RELATIONS_ORDER_FORM_NEEDED("Order Form Needed", 322),

        PLATFORM_GENERAL("General", 336),
        PLATFORM_NETWORK("Network", 337),
        PLATFORM_HOST_HARDWARE("Host/Hardware", 338),
        PLATFORM_STORAGE("Storage", 339),
        PLATFORM_SOFTWARE("Software", 340),
        PLATFORM_VENDOR_ISSUE("Vendor Issue", 341);

        private static final Map<Long, Type> typesById = uniqueIndex(asList(Type.values()), Type::getId);
        private final String name;
        private final long id;

        public static Type byId(long id) {
            return typesById.get(id);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH;

        @Override
        public String toString() {
            return capitalize(name().toLowerCase());
        }
    }

    public enum Impact {
        Low,
        Medium,
        High;

        @Override
        public String toString() {
            return name();
        }
    }
}
