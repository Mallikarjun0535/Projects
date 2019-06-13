package com.dizzion.portal.domain.announcement.web;

import com.dizzion.portal.domain.announcement.AnnouncementService;
import com.dizzion.portal.domain.announcement.dto.Announcement;
import com.dizzion.portal.domain.announcement.dto.AnnouncementUpdateRequest;
import com.dizzion.portal.domain.announcement.dto.AttachmentUrl;
import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.security.resource.AnnouncementPermissionsResolver;
import com.dizzion.portal.security.resource.ResourceWithPermissions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Set;

import static com.dizzion.portal.domain.role.Permission.Constants.EDIT_ANNOUNCEMENTS;
import static com.dizzion.portal.domain.role.Permission.Constants.VIEW_ANNOUNCEMENTS;

@RestController
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final AnnouncementPermissionsResolver permissionsResolver;

    public AnnouncementController(AnnouncementService announcementService, AnnouncementPermissionsResolver permissionsResolver) {
        this.announcementService = announcementService;
        this.permissionsResolver = permissionsResolver;
    }

    @GetMapping("/announcements")
    @Secured(VIEW_ANNOUNCEMENTS)
    public Page<ResourceWithPermissions<Announcement>> getPage(Pageable pageRequest, Set<FieldFilter> filters) {
        return announcementService.getPage(pageRequest, filters).map(permissionsResolver::enrichWithPermissions);
    }

    @PostMapping("/announcements")
    @Secured(EDIT_ANNOUNCEMENTS)
    public Announcement preCreate() {
        return announcementService.preCreate();
    }

    @PutMapping("/announcements/{id}")
    @Secured(EDIT_ANNOUNCEMENTS)
    public Announcement update(@PathVariable long id, @RequestBody @Valid AnnouncementUpdateRequest announcement) {
        return announcementService.update(id, announcement);
    }

    @PostMapping("/announcements/{id}/attachments")
    @Secured(EDIT_ANNOUNCEMENTS)
    public AttachmentUrl uploadAttachment(@PathVariable long id, @RequestParam("upload") MultipartFile file) {
        return new AttachmentUrl(announcementService.uploadAttachment(id, file));
    }

    @DeleteMapping("/announcements/{id}")
    @Secured(EDIT_ANNOUNCEMENTS)
    public void delete(@PathVariable long id) {
        announcementService.delete(id);
    }
}
