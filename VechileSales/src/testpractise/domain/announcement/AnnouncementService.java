package com.dizzion.portal.domain.announcement;

import com.dizzion.portal.domain.announcement.dto.Announcement;
import com.dizzion.portal.domain.announcement.dto.AnnouncementUpdateRequest;
import com.dizzion.portal.domain.announcement.persistence.entity.AnnouncementEntity;
import com.dizzion.portal.domain.attachment.AttachmentService;
import com.dizzion.portal.domain.common.AbstractCrudService;
import com.dizzion.portal.domain.common.ScopedEntityService;
import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import com.google.common.collect.ImmutableList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

@Service
@Slf4j
@Transactional
public class AnnouncementService extends AbstractCrudService<AnnouncementEntity> {

    private final AttachmentService attachmentService;
    private final AuthenticatedUserAccessor auth;
    private final String bucketName;

    public AnnouncementService(AuthenticatedUserAccessor auth,
                               ScopedEntityService scopedEntityService,
                               AttachmentService attachmentService,
                               @Value("${aws.announcements.s3-bucket-name}") String bucketName) {
        super(scopedEntityService);
        this.auth = auth;
        this.attachmentService = attachmentService;
        this.bucketName = bucketName;
    }

    @Transactional(readOnly = true)
    public Page<Announcement> getPage(Pageable pageRequest, Set<FieldFilter> filters) {
        return getEntitiesPage(pageRequest, filters).map(Announcement::from);
    }

    public Announcement preCreate() {
        return Announcement.from(save(AnnouncementEntity.builder()
                .title("New Announcement")
                .pages(ImmutableList.of(""))
                .startDate(now())
                .endDate(now())
                .organizations(emptySet())
                .owner(scopedEntityService.getForWrite(auth.getOrganization().getId(), OrganizationEntity.class))
                .build()));
    }

    public Announcement update(long id, AnnouncementUpdateRequest announcement) {
        AnnouncementEntity existing = getForWrite(id);
        existing.setTitle(announcement.getTitle());
        existing.setPages(asList(announcement.getPages()));
        existing.setStartDate(announcement.getStartDate());
        existing.setEndDate(announcement.getEndDate());
        existing.setOrganizations(scopedEntityService.getForWrite(announcement.getOrganizationIds(), OrganizationEntity.class));
        return Announcement.from(save(existing));
    }

    public String uploadAttachment(long announcementId, MultipartFile file) {
        checkAccess(announcementId);
        return attachmentService.uploadPublicAttachment(bucketName, announcementId, file);
    }

    @Override
    public void delete(long id) {
        super.delete(id);
        attachmentService.deletePublicAttachment(bucketName, id);
    }
}
