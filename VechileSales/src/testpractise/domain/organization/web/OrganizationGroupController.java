package com.dizzion.portal.domain.organization.web;

import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.organization.OrganizationGroupService;
import com.dizzion.portal.domain.organization.dto.OrganizationGroup;
import com.dizzion.portal.domain.organization.dto.OrganizationGroupCreateUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

import static com.dizzion.portal.domain.role.Permission.Constants.EDIT_ORGANIZATION_GROUPS;
import static com.dizzion.portal.domain.role.Permission.Constants.VIEW_ORGANIZATION_GROUP_MANAGEMENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class OrganizationGroupController {

    private final OrganizationGroupService orgGroupService;

    public OrganizationGroupController(OrganizationGroupService orgGroupService) {
        this.orgGroupService = orgGroupService;
    }

    @RequestMapping(path = "/organization-groups", method = GET)
    @Secured(VIEW_ORGANIZATION_GROUP_MANAGEMENT)
    public Page<OrganizationGroup> getPage(Pageable pageRequest, Set<FieldFilter> filters) {
        return orgGroupService.getPage(pageRequest, filters);
    }

    @RequestMapping(path = "/organization-groups/name/uniqueness", method = GET)
    @Secured(EDIT_ORGANIZATION_GROUPS)
    public boolean checkNameUnique(String name) {
        return orgGroupService.isNameAvailable(name);
    }

    @RequestMapping(path = "/organization-groups", method = POST)
    @Secured(EDIT_ORGANIZATION_GROUPS)
    public OrganizationGroup create(@RequestBody @Valid OrganizationGroupCreateUpdateRequest orgGroup) {
        return orgGroupService.create(orgGroup);
    }

    @RequestMapping(path = "/organization-groups/{id}", method = PUT)
    @Secured(EDIT_ORGANIZATION_GROUPS)
    public OrganizationGroup update(@PathVariable long id, @RequestBody @Valid OrganizationGroupCreateUpdateRequest orgGroup) {
        return orgGroupService.update(id, orgGroup);
    }

    @RequestMapping(path = "/organization-groups/{id}", method = DELETE)
    @Secured(EDIT_ORGANIZATION_GROUPS)
    public void delete(@PathVariable long id) {
        orgGroupService.delete(id);
    }
}
