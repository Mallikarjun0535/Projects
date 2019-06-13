package com.dizzion.portal.domain.maintenance.web;

import com.dizzion.portal.domain.announcement.dto.AttachmentUrl;
import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.helpdesk.dto.TicketAttachment;
import com.dizzion.portal.domain.helpdesk.dto.TicketComment;
import com.dizzion.portal.domain.helpdesk.dto.TicketCommentCreateUpdateRequest;
import com.dizzion.portal.domain.maintenance.MaintenanceEventService;
import com.dizzion.portal.domain.maintenance.dto.MaintenanceEvent;
import com.dizzion.portal.domain.maintenance.dto.MaintenanceEventProgressUpdateRequest;
import com.dizzion.portal.domain.maintenance.dto.MaintenanceEventUpdateRequest;
import com.dizzion.portal.domain.maintenance.dto.MaintenanceEventUserAssignmentRequest;
import com.dizzion.portal.security.resource.MaintenanceEventPermissionsResolver;
import com.dizzion.portal.security.resource.ResourceWithPermissions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.dizzion.portal.domain.role.Permission.Constants.EDIT_MAINTENANCE_EVENTS;
import static com.dizzion.portal.domain.role.Permission.Constants.VIEW_MAINTENANCE_EVENTS;
import static java.util.stream.Collectors.toList;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@RestController
public class MaintenanceEventController {

    private final MaintenanceEventService maintenanceEventService;
    private final MaintenanceEventPermissionsResolver permissionsResolver;

    public MaintenanceEventController(MaintenanceEventService maintenanceEventService,
                                      MaintenanceEventPermissionsResolver permissionsResolver) {
        this.maintenanceEventService = maintenanceEventService;
        this.permissionsResolver = permissionsResolver;
    }

    @GetMapping("/maintenance-events")
    @Secured(VIEW_MAINTENANCE_EVENTS)
    public Page<ResourceWithPermissions<MaintenanceEvent>> getPage(Pageable pageRequest, Set<FieldFilter> filters) {
        System.out.println("+++++11111111111111111111111111111");
        return maintenanceEventService.getPage(pageRequest, filters).map(permissionsResolver::enrichWithPermissions);
    }

    @GetMapping("/maintenance-events/all/date-range")
    @Secured(VIEW_MAINTENANCE_EVENTS)
    public List<ResourceWithPermissions<MaintenanceEvent>> getAllScheduledJobsInDateRange(@RequestParam @DateTimeFormat(iso = DATE) LocalDate from,
                                                                                          @RequestParam @DateTimeFormat(iso = DATE) LocalDate until) {
        return maintenanceEventService.getAllScheduledJobsInstancesBetween(from, until).stream()
                .map(permissionsResolver::enrichWithPermissions)
                .collect(toList());
    }

    @GetMapping("/maintenance-events/maintenance/date-range")
    @Secured(VIEW_MAINTENANCE_EVENTS)
    public List<ResourceWithPermissions<MaintenanceEvent>> getMaintenanceEventsInDateRange(@RequestParam @DateTimeFormat(iso = DATE) LocalDate from,
                                                                                           @RequestParam @DateTimeFormat(iso = DATE) LocalDate until) {
        return maintenanceEventService.getMaintenanceEventInstancesBetween(from, until).stream()
                .map(permissionsResolver::enrichWithPermissions)
                .collect(toList());
    }


    @GetMapping("/maintenance-events/{id}")
    @Secured(VIEW_MAINTENANCE_EVENTS)
    public ResourceWithPermissions<MaintenanceEvent> get(@PathVariable long id, @RequestParam @DateTimeFormat(iso = DATE) Optional<LocalDate> startDate) {
        return permissionsResolver.enrichWithPermissions(startDate
                .map(date -> maintenanceEventService.getMaintenanceEventInstance(id, date))
                .orElseGet(() -> maintenanceEventService.getMaintenanceEvent(id)));
    }

    @GetMapping("/maintenance-events/{id}/next-occurrence")
    @Secured(VIEW_MAINTENANCE_EVENTS)
    public MaintenanceEvent getNextOccurrence(@PathVariable long id) {
        return maintenanceEventService.getNextOccurrenceInstance(id);
    }

    @PostMapping("/maintenance-events")
    @Secured(EDIT_MAINTENANCE_EVENTS)
    public MaintenanceEvent create() {
        return maintenanceEventService.preCreate();
    }

    @PutMapping("/maintenance-events/{id}")
    @Secured(EDIT_MAINTENANCE_EVENTS)
    public MaintenanceEvent update(
            @PathVariable long id,
            @RequestPart("json") @Valid MaintenanceEventUpdateRequest maintenanceEvent,
            @RequestPart("file") Optional<MultipartFile[]> file) {
        return maintenanceEventService.update(id, maintenanceEvent, file);
    }

    @PostMapping("/maintenance-events/{id}/attachments")
    @Secured(EDIT_MAINTENANCE_EVENTS)
    public AttachmentUrl uploadAttachment(@PathVariable long id, @RequestParam("upload") MultipartFile file) {
        return new AttachmentUrl(maintenanceEventService.uploadAttachment(id, file));
    }

    @GetMapping("/maintenance-events/{id}/comments")
    @Secured(EDIT_MAINTENANCE_EVENTS)
    public List<TicketComment> getComments(@PathVariable long id,
                                           @RequestParam @DateTimeFormat(iso = DATE) LocalDate startDate) {
        return maintenanceEventService.getComments(id, startDate);
    }

    @PostMapping("/maintenance-events/{id}/comments")
    @Secured(EDIT_MAINTENANCE_EVENTS)
    public void addComment(@PathVariable long id,
                           @RequestParam @DateTimeFormat(iso = DATE) LocalDate startDate,
                           @RequestPart("json") @Valid TicketCommentCreateUpdateRequest comment,
                           @RequestPart("file") Optional<MultipartFile> attachment) {
        maintenanceEventService.addComment(id, startDate, comment, attachment);
    }

    @GetMapping("/maintenance-events/{id}/comments/attachments")
    @Secured(VIEW_MAINTENANCE_EVENTS)
    public List<TicketAttachment> getProgressTicketAttachments(@PathVariable long id,
                                                               @RequestParam @DateTimeFormat(iso = DATE) LocalDate startDate) {
        return maintenanceEventService.getProgressTicketAttachments(id, startDate);
    }

    @DeleteMapping("/maintenance-events/{id}")
    @Secured(EDIT_MAINTENANCE_EVENTS)
    public void delete(@PathVariable long id, @RequestParam @DateTimeFormat(iso = DATE) Optional<LocalDate> startDate) {
        if (startDate.isPresent()) {
            maintenanceEventService.delete(id, startDate.get());
        } else {
            maintenanceEventService.delete(id);
        }
    }

    @PostMapping("/maintenance-events/{id}/approval")
    @Secured(EDIT_MAINTENANCE_EVENTS)
    public MaintenanceEvent approve(@PathVariable long id) {
        return maintenanceEventService.approve(id);
    }

    @DeleteMapping("/maintenance-events/{id}/approval")
    @Secured(EDIT_MAINTENANCE_EVENTS)
    public MaintenanceEvent reject(@PathVariable long id) {
        return maintenanceEventService.reject(id);
    }

    @PutMapping("/maintenance-events/{id}/status")
    @Secured(EDIT_MAINTENANCE_EVENTS)
    public MaintenanceEvent updateStatus(@PathVariable long id, @RequestBody MaintenanceEventProgressUpdateRequest request) {
        return maintenanceEventService.updateProgress(id, request);
    }

    @PutMapping("/maintenance-events/{id}/assigned-user")
    @Secured(EDIT_MAINTENANCE_EVENTS)
    public MaintenanceEvent assignUser(@PathVariable long id, @RequestBody MaintenanceEventUserAssignmentRequest request) {
        return maintenanceEventService.assignUser(id, request);
    }
}
