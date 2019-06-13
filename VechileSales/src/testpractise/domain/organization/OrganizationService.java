package com.dizzion.portal.domain.organization;

import com.dizzion.portal.domain.application.persistence.entity.ApplicationEntity;
import com.dizzion.portal.domain.common.AbstractCrudService;
import com.dizzion.portal.domain.common.ScopedEntityService;
import com.dizzion.portal.domain.exception.EntityNotFoundException;
import com.dizzion.portal.domain.exception.UniqueConstraintException;
import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.organization.dto.Organization;
import com.dizzion.portal.domain.organization.dto.Organization.OrganizationType;
import com.dizzion.portal.domain.organization.dto.OrganizationCreateRequest;
import com.dizzion.portal.domain.organization.dto.OrganizationUpdateRequest;
import com.dizzion.portal.domain.organization.dto.SupportContacts;
import com.dizzion.portal.domain.organization.persistence.OrganizationGroupRepository;
import com.dizzion.portal.domain.organization.persistence.OrganizationRepository;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.user.dto.ShortUserInfo;
import com.dizzion.portal.domain.user.persistence.UserRepository;
import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import com.google.common.primitives.Longs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.dizzion.portal.domain.organization.dto.Organization.OrganizationType.*;
import static com.dizzion.portal.domain.scope.TenantPathUtils.tenantPath;
import static com.dizzion.portal.domain.scope.TenantPathUtils.tenantScope;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

@Service
@Transactional
public class OrganizationService extends AbstractCrudService<OrganizationEntity> {

    private final AuthenticatedUserAccessor auth;
    private final OrganizationRepository orgRepo;
    private final OrganizationGroupRepository orgGroupRepo;
    private final UserRepository userRepository;

    public OrganizationService(AuthenticatedUserAccessor auth,
                               OrganizationRepository orgRepo,
                               ScopedEntityService scopedEntityService,
                               OrganizationGroupRepository orgGroupRepo,
                               UserRepository userRepository) {
        super(scopedEntityService);
        this.auth = auth;
        this.orgRepo = orgRepo;
        this.orgGroupRepo = orgGroupRepo;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<Organization> getPage(Pageable pageRequest, Set<FieldFilter> filters) {
        return getEntitiesPage(pageRequest, filters).map(Organization::from);
    }

    @Transactional(readOnly = true)
    public Set<Organization> getOrganizations(long... ids) {
        return scopedEntityService.getForRead(Longs.asList(ids), OrganizationEntity.class).stream()
                .map(Organization::from)
                .collect(toSet());
    }

    @Transactional(readOnly = true)
    public Organization getOrganization(long orgId) {
        return Organization.from(getForRead(orgId));
    }

    @Transactional(readOnly = true)
    public Optional<Organization> getOrganizationByCustomerId(String customerId) {
        return orgRepo.findByCustomerIdAndTenantPathLike(customerId, tenantScope(auth.getTenantPath()))
                .map(Organization::from);
    }

    @Transactional(readOnly = true)
    public Organization getDizzionOrganization() {
        List<OrganizationEntity> organizations = orgRepo.findByTypeAndTenantPathLike(DIZZION, tenantScope(auth.getTenantPath()));
        if (organizations.size() != 1) {
            throw new IllegalStateException();
        }
        return Organization.from(organizations.get(0));
    }

    @Transactional(readOnly = true)
    public SupportContacts getSupportContacts(long id) {
        OrganizationEntity org = getForRead(id);
        return SupportContacts.builder()
                .supportPhoneNumber(org.getSupportPhoneNumber())
                .customerRelationshipManager(org.getCustomerRelationshipManager().map(ShortUserInfo::from))
                .serviceDeliveryManager(org.getServiceDeliveryManager().map(ShortUserInfo::from))
                .build();
    }

    public Organization create(OrganizationCreateRequest org) {
        checkAbilityToCreateOrgWithOrgType(org.getType());
        if (!isNameAvailable(org.getName())) {
            throw new UniqueConstraintException("name");
        }
        checkCompliantOrgHasTwoFactorAuthEnabled(org.getCompliant(), org.getTwoFactorAuth());

        OrganizationEntity parent = getForWrite(org.getParentOrganizationId());
        if (parent.getType() == PORTAL_ADMIN || parent.getType() == CUSTOMER) {
            throw new IllegalArgumentException(
                    format("Cannot create a direct child of organization of type=%s. createRequest=%s", parent.getType(), org));
        }

        UserEntity crm = org.getCustomerRelationshipManagerId()
                .map(userId -> scopedEntityService.getForRead(userId, UserEntity.class))
                .map(this::checkHasPhone)
                .orElse(null);
        UserEntity sdm = org.getServiceDeliveryManagerId()
                .map(userId -> scopedEntityService.getForRead(userId, UserEntity.class))
                .map(this::checkHasPhone)
                .orElse(null);
        Set<ApplicationEntity> starredApps = scopedEntityService.getForRead(org.getStarredApplicationIds(), ApplicationEntity.class);

        OrganizationEntity saved = save(OrganizationEntity.builder()
                .name(org.getName())
                .customerId(org.getCustomerId())
                .parent(parent)
                .tenantPath("")
                .type(org.getType())
                .enabled(org.getEnabled())
                .compliant(org.getCompliant())
                .twoFactorAuth(org.getTwoFactorAuth())
                .features(org.getTicketing().asFeatures())
                .supportPhoneNumber(org.getSupportPhoneNumber().orElse(null))
                .customerRelationshipManager(crm)
                .serviceDeliveryManager(sdm)
                .starredApplications(starredApps)
                .build()
        );

        saved.setTenantPath(tenantPath(parent.getTenantPath(), saved.getId()));
        return Organization.from(save(saved));
    }

    public Organization update(long id, OrganizationUpdateRequest org) {
        checkNotSelf(id);
        checkCompliantOrgHasTwoFactorAuthEnabled(org.getCompliant(), org.getTwoFactorAuth());

        OrganizationEntity existing = getForWrite(id);

        String oldName = existing.getName();
        String newName = org.getName();
        if (!oldName.equals(newName) && !isNameAvailable(newName)) {
            throw new UniqueConstraintException("name");
        }
        String oldCid = existing.getCustomerId();
        String newCid = org.getCustomerId();
        if (!oldCid.equals(newCid) && isCustomerIdTaken(newCid)) {
            throw new UniqueConstraintException("customerId");
        }

        UserEntity crm = org.getCustomerRelationshipManagerId()
                .map(userId -> scopedEntityService.getForRead(userId, UserEntity.class))
                .map(this::checkHasPhone)
                .map(user -> checkHasDifferentOrg(user, id))
                .orElse(null);
        UserEntity sdm = org.getServiceDeliveryManagerId()
                .map(userId -> scopedEntityService.getForRead(userId, UserEntity.class))
                .map(this::checkHasPhone)
                .map(user -> checkHasDifferentOrg(user, id))
                .orElse(null);
        Set<ApplicationEntity> starredApps = scopedEntityService.getForRead(org.getStarredApplicationIds(), ApplicationEntity.class);

        existing.setCustomerId(org.getCustomerId());
        existing.setName(org.getName());
        existing.setEnabled(org.getEnabled());
        existing.setCompliant(org.getCompliant());
        existing.setTwoFactorAuth(org.getTwoFactorAuth());
        existing.setFeatures(org.getTicketing().asFeatures());
        existing.setSupportPhoneNumber(org.getSupportPhoneNumber().orElse(null));
        existing.setCustomerRelationshipManager(crm);
        existing.setServiceDeliveryManager(sdm);
        existing.setStarredApplications(starredApps);
        return Organization.from(save(existing));
    }

    @Override
    @Transactional
    public void delete(long id) {
        checkNotSelf(id);
        super.delete(id);
    }

    @Transactional(readOnly = true)
    public boolean isNameAvailable(String name) {
        return orgRepo.findByName(name) == null && orgGroupRepo.findByName(name) == null;
    }

    @Transactional(readOnly = true)
    public boolean isCustomerIdTaken(String customerId) {
        return orgRepo.findByCustomerId(customerId).isPresent();
    }

    private void checkNotSelf(long id) {
        if (id == auth.getOrganization().getId()) {
            throw new EntityNotFoundException();
        }
    }

    private void checkCompliantOrgHasTwoFactorAuthEnabled(boolean compliant, boolean twoFactorAuthEnabled) {
        if (compliant && !twoFactorAuthEnabled) {
            throw new IllegalArgumentException("Compliant org must have two-factor auth enabled");
        }
    }

    private void checkAbilityToCreateOrgWithOrgType(OrganizationType type) {
        if (type == PORTAL_ADMIN || !auth.getOrganization().getType().getAvailableTypes().contains(type)) {
            throw new IllegalArgumentException();
        }
    }

    private UserEntity checkHasPhone(UserEntity user) {
        if (!user.getWorkPhoneNumber().isPresent() && !user.getMobilePhoneNumber().isPresent()) {
            throw new IllegalArgumentException();
        }
        return user;
    }

    private UserEntity checkHasDifferentOrg(UserEntity user, long orgId) {
        if (user.getOrganization().getId() == orgId) {
            throw new IllegalArgumentException();
        }
        return user;
    }
}
