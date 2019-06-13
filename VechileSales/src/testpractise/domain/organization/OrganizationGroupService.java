package com.dizzion.portal.domain.organization;

import com.dizzion.portal.domain.common.AbstractCrudService;
import com.dizzion.portal.domain.common.ScopedEntityService;
import com.dizzion.portal.domain.exception.UniqueConstraintException;
import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.organization.dto.OrganizationGroup;
import com.dizzion.portal.domain.organization.dto.OrganizationGroupCreateUpdateRequest;
import com.dizzion.portal.domain.organization.persistence.OrganizationGroupRepository;
import com.dizzion.portal.domain.organization.persistence.OrganizationRepository;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationGroupEntity;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class OrganizationGroupService extends AbstractCrudService<OrganizationGroupEntity> {

    private final AuthenticatedUserAccessor auth;
    private final OrganizationRepository orgRepo;
    private final OrganizationGroupRepository orgGroupRepo;

    public OrganizationGroupService(AuthenticatedUserAccessor auth,
                                    ScopedEntityService scopedEntityService, OrganizationRepository orgRepo, OrganizationGroupRepository orgGroupRepo) {
        super(scopedEntityService);
        this.auth = auth;
        this.orgRepo = orgRepo;
        this.orgGroupRepo = orgGroupRepo;
    }

    @Transactional(readOnly = true)
    public Page<OrganizationGroup> getPage(Pageable pageRequest, Set<FieldFilter> filters) {
        return getEntitiesPage(pageRequest, filters).map(OrganizationGroup::from);
    }

    @Transactional(readOnly = true)
    public boolean isNameAvailable(String name) {
        return orgGroupRepo.findByName(name) == null && orgRepo.findByName(name) == null;
    }

    public OrganizationGroup create(OrganizationGroupCreateUpdateRequest orgGroup) {
        if (!isNameAvailable(orgGroup.getName())) {
            throw new UniqueConstraintException("name");
        }

        OrganizationGroupEntity entity = OrganizationGroupEntity.builder()
                .name(orgGroup.getName())
                .organizations(scopedEntityService.getForWrite(orgGroup.getOrganizationIds(), OrganizationEntity.class))
                .owner(scopedEntityService.getForWrite(auth.getOrganization().getId(), OrganizationEntity.class))
                .build();
        return OrganizationGroup.from(save(entity));
    }

    public OrganizationGroup update(long id, OrganizationGroupCreateUpdateRequest orgGroup) {
        OrganizationGroupEntity existing = getForWrite(id);

        String oldName = existing.getName();
        String newName = orgGroup.getName();
        if (!oldName.equals(newName) && !isNameAvailable(newName)) {
            throw new UniqueConstraintException("name");
        }

        existing.setName(orgGroup.getName());
        existing.setOrganizations(scopedEntityService.getForWrite(orgGroup.getOrganizationIds(), OrganizationEntity.class));
        return OrganizationGroup.from(save(existing));
    }
}
