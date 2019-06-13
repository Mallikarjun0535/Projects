package com.dizzion.portal.domain.notification.web;

import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.notification.NotificationService;
import com.dizzion.portal.domain.notification.dto.Notification;
import com.dizzion.portal.domain.notification.dto.NotificationCreateUpdateRequest;
import com.dizzion.portal.security.resource.NotificationPermissionsResolver;
import com.dizzion.portal.security.resource.ResourceWithPermissions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;

import static com.dizzion.portal.domain.role.Permission.Constants.EDIT_NOTIFICATIONS;
import static com.dizzion.portal.domain.role.Permission.Constants.VIEW_NOTIFICATIONS;

@RestController
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationPermissionsResolver permissionsResolver;

    public NotificationController(NotificationService notificationService, NotificationPermissionsResolver permissionsResolver) {
        this.notificationService = notificationService;
        this.permissionsResolver = permissionsResolver;
    }

    @GetMapping("/notifications")
    @Secured(VIEW_NOTIFICATIONS)
    public Page<ResourceWithPermissions<Notification>> getPage(Pageable pageRequest, Set<FieldFilter> filters) {
        return notificationService.getPage(pageRequest, filters).map(permissionsResolver::enrichWithPermissions);
    }

    @PostMapping("/notifications")
    @Secured(EDIT_NOTIFICATIONS)
    public Notification create(@RequestBody @Valid NotificationCreateUpdateRequest notification) {
        return notificationService.create(notification);
    }

    @PutMapping("/notifications/{id}")
    @Secured(EDIT_NOTIFICATIONS)
    public Notification update(@PathVariable long id, @RequestBody @Valid NotificationCreateUpdateRequest notification) {
        return notificationService.update(id, notification);
    }

    @DeleteMapping("/notifications/{id}")
    @Secured(EDIT_NOTIFICATIONS)
    public void delete(@PathVariable long id) {
        notificationService.delete(id);
    }

}
