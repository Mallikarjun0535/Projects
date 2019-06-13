package com.dizzion.portal.domain.notification;

import com.dizzion.portal.domain.common.AbstractCrudService;
import com.dizzion.portal.domain.common.ScopedEntityService;
import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.notification.dto.Notification;
import com.dizzion.portal.domain.notification.dto.NotificationCreateUpdateRequest;
import com.dizzion.portal.domain.notification.persistence.entity.NotificationEntity;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class NotificationService extends AbstractCrudService<NotificationEntity> {

    private final AuthenticatedUserAccessor auth;

    public NotificationService(AuthenticatedUserAccessor auth, ScopedEntityService entityService) {
        super(entityService);
        this.auth = auth;
    }

    @Transactional(readOnly = true)
    public Page<Notification> getPage(Pageable pageRequest, Set<FieldFilter> filters) {
        return getEntitiesPage(pageRequest, filters).map(Notification::from);
    }

    public Notification create(NotificationCreateUpdateRequest notification) {
        NotificationEntity entity = NotificationEntity.builder()
                .title(notification.getTitle())
                .message(notification.getMessage())
                .startDate(notification.getStartDate())
                .endDate(notification.getEndDate())
                .organizations(scopedEntityService.getForWrite(notification.getOrganizationIds(), OrganizationEntity.class))
                .owner(scopedEntityService.getForWrite(auth.getOrganization().getId(), OrganizationEntity.class))
                .build();
        return Notification.from(save(entity));
    }

    public Notification update(long id, NotificationCreateUpdateRequest notification) {
        NotificationEntity existing = getForWrite(id);
        existing.setTitle(notification.getTitle());
        existing.setMessage(notification.getMessage());
        existing.setStartDate(notification.getStartDate());
        existing.setEndDate(notification.getEndDate());
        existing.setOrganizations(scopedEntityService.getForWrite(notification.getOrganizationIds(), OrganizationEntity.class));
        return Notification.from(save(existing));
    }
}
