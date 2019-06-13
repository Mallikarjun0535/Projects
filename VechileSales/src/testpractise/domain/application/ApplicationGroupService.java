package com.dizzion.portal.domain.application;

import com.dizzion.portal.domain.application.dto.ApplicationGroup;
import com.dizzion.portal.domain.application.dto.ApplicationGroupCreateUpdateRequest;
import com.dizzion.portal.domain.application.persistence.ApplicationGroupRepository;
import com.dizzion.portal.domain.application.persistence.entity.ApplicationGroupEntity;
import com.dizzion.portal.domain.common.AbstractCrudService;
import com.dizzion.portal.domain.common.ScopedEntityService;
import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import com.dizzion.portal.security.resource.OrganizationPermissionsResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class ApplicationGroupService extends AbstractCrudService<ApplicationGroupEntity> {

    private final AuthenticatedUserAccessor auth;
    private final ApplicationGroupRepository appGroupRepo;
    private final OrganizationPermissionsResolver orgPermResolver;

    public ApplicationGroupService(AuthenticatedUserAccessor auth,
                                   OrganizationPermissionsResolver orgPermResolver,
                                   ScopedEntityService scopedEntityService,
                                   ApplicationGroupRepository appGroupRepo) {
        super(scopedEntityService);
        this.auth = auth;
        this.orgPermResolver = orgPermResolver;
        this.appGroupRepo = appGroupRepo;
    }

    @Transactional(readOnly = true)
    public Page<ApplicationGroup> getPage(Pageable pageRequest, Set<FieldFilter> filters) {
        return getEntitiesPage(pageRequest, filters)
                .map(appGroup -> ApplicationGroup.fromWithOrgFilter(appGroup, orgPermResolver::isEditable));
    }

    @Transactional(readOnly = true)
    public boolean isNameAvailable(String name) {
        return appGroupRepo.findByName(name) == null;
    }

    public ApplicationGroup create(ApplicationGroupCreateUpdateRequest appGroup) {
        ApplicationGroupEntity entity = ApplicationGroupEntity.builder()
                .name(appGroup.getName())
                .enabled(appGroup.getEnabled())
                .organizations(scopedEntityService.getForWrite(appGroup.getOrganizationIds(), OrganizationEntity.class))
                .owner(scopedEntityService.getForWrite(auth.getOrganization().getId(), OrganizationEntity.class))
                .build();
        return ApplicationGroup.from(save(entity));
    }

    public ApplicationGroup update(long id, ApplicationGroupCreateUpdateRequest appGroup) {
        ApplicationGroupEntity existing = getForWrite(id);
        existing.setName(appGroup.getName());
        existing.setEnabled(appGroup.getEnabled());
        existing.setOrganizations(scopedEntityService.getForWrite(appGroup.getOrganizationIds(), OrganizationEntity.class));
        return ApplicationGroup.from(save(existing));
    }
}
