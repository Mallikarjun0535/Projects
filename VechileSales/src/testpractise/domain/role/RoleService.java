package com.dizzion.portal.domain.role;

import com.dizzion.portal.domain.exception.EntityNotFoundException;
import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.filter.persistence.FilterSpecificationFactory;
import com.dizzion.portal.domain.organization.dto.Organization.OrganizationType;
import com.dizzion.portal.domain.role.dto.Role;
import com.dizzion.portal.domain.role.dto.RoleCreateUpdateRequest;
import com.dizzion.portal.domain.role.persistence.RoleRepository;
import com.dizzion.portal.domain.role.persistence.entity.RoleEntity;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.Set;

import static com.google.common.collect.Iterables.tryFind;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

@Service
@Transactional
public class RoleService {

    private final RoleRepository roleRepo;
    private final AuthenticatedUserAccessor auth;
    private final FilterSpecificationFactory<RoleEntity> filterSpecFactory;

    public RoleService(RoleRepository roleRepo, AuthenticatedUserAccessor auth, FilterSpecificationFactory<RoleEntity> filterSpecFactory) {
        this.roleRepo = roleRepo;
        this.auth = auth;
        this.filterSpecFactory = filterSpecFactory;
    }

    @Transactional(readOnly = true)
    public Page<Role> getRoles(Pageable pageRequest, Set<FieldFilter> filters) {
        return roleRepo.findAll(asSpecification(filters), pageRequest).map(Role::from);
    }

    @Transactional(readOnly = true)
    public Role getRole(String name) {
        Role role = Role.from(roleRepo.findByName(name));
        if (getSubordinateRoles(auth.getAuthenticatedUser().getUser().getRole().getName()).contains(role)) {
            return role;
        } else {
            throw new EntityNotFoundException();
        }
    }

    @Transactional(readOnly = true)
    public Set<Permission> getPermissions() {
        return auth.getAuthenticatedUser().getUser().getRole().getPermissions();
    }

    @Transactional(readOnly = true)
    public Set<Role> getSubordinateRoles(String roleName) {
        Set<String> authSubordinateRoles = roleRepo
                .findSubordinateRoles(auth.getAuthenticatedUser().getUser().getRole().getName()).stream()
                .map(RoleEntity::getName)
                .collect(toSet());

        if (authSubordinateRoles.contains(roleName)) {
            return roleRepo.findSubordinateRoles(roleName).stream()
                    .map(Role::from)
                    .collect(toSet());
        } else {
            throw new EntityNotFoundException();
        }
    }

    @Transactional(readOnly = true)
    public Set<Role> getRolesAvailableForOrganizationType(String roleName, OrganizationType organizationType) {
        return getSubordinateRoles(roleName).stream()
                .filter(role -> role.getOrganizationTypes().contains(organizationType))
                .collect(toSet());
    }

    public Role create(RoleCreateUpdateRequest createRequest) {
        RoleEntity entity = RoleEntity.builder()
                .name(createRequest.getName())
                .permissions(createRequest.getPermissions())
                .organizationTypes(createRequest.getOrganizationTypes())
                .build();
        return Role.from(roleRepo.save(entity));
    }

    public Role update(long id, RoleCreateUpdateRequest updateRequest) {
        RoleEntity existing = roleRepo.findOne(id);
        existing.setName(updateRequest.getName());
        existing.setPermissions(updateRequest.getPermissions());
        existing.setOrganizationTypes(updateRequest.getOrganizationTypes());
        return Role.from(roleRepo.save(existing));
    }

    public void delete(long id) {
        roleRepo.delete(id);
    }

    @Transactional(readOnly = true)
    public boolean isNameAvailable(String name) {
        return roleRepo.findByName(name) == null;
    }

    private Specification<RoleEntity> asSpecification(Set<FieldFilter> filters) {
        FieldFilter permissionsFilter = tryFind(filters, filter -> "permissions".equals(filter.getKey())).orNull();
        FieldFilter orgTypesFilter = tryFind(filters, filter -> "organizationTypes".equals(filter.getKey())).orNull();

        filters.remove(permissionsFilter);
        filters.remove(orgTypesFilter);

        Specifications<RoleEntity> filterSpec = filterSpecFactory.specificationFor(RoleEntity.class, filters);
        if (permissionsFilter != null) {
            filterSpec = filterSpec.and(permissionsSpec(permissionsFilter));
        }
        if (orgTypesFilter != null) {
            filterSpec = filterSpec.and(orgTypesSpec(orgTypesFilter));
        }
        return filterSpec;
    }

    private Specification<RoleEntity> permissionsSpec(FieldFilter roleNameFilter) {
        return (roleRoot, query, cb) -> {
            Predicate[] predicates = stream(roleNameFilter.getValue().split(","))
                    .map(Permission::valueOf)
                    .map(permission -> cb.isMember(permission, roleRoot.get("permissions")))
                    .toArray(Predicate[]::new);
            return cb.and(predicates);
        };
    }

    private Specification<RoleEntity> orgTypesSpec(FieldFilter orgTypesFilter) {
        return (roleRoot, query, cb) -> {
            Predicate[] predicates = stream(orgTypesFilter.getValue().split(","))
                    .map(OrganizationType::valueOf)
                    .map(type -> cb.isMember(type, roleRoot.get("organizationTypes")))
                    .toArray(Predicate[]::new);
            return cb.and(predicates);
        };
    }
}
