package com.dizzion.portal.domain.organization.web;

import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.organization.OrganizationService;
import com.dizzion.portal.domain.organization.dto.Organization;
import com.dizzion.portal.domain.organization.dto.OrganizationCreateRequest;
import com.dizzion.portal.domain.organization.dto.OrganizationUpdateRequest;
import com.dizzion.portal.domain.organization.dto.SupportContacts;
import com.dizzion.portal.security.resource.OrganizationPermissionsResolver;
import com.dizzion.portal.security.resource.ResourceWithPermissions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

import static com.dizzion.portal.domain.role.Permission.Constants.EDIT_ORGANIZATIONS;
import static com.dizzion.portal.domain.role.Permission.Constants.VIEW_ORGANIZATION_MANAGEMENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class OrganizationController {

    private final OrganizationService orgService;
    private final OrganizationPermissionsResolver permissionsResolver;

    public OrganizationController(OrganizationService orgService, OrganizationPermissionsResolver permissionsResolver) {
        this.orgService = orgService;
        this.permissionsResolver = permissionsResolver;
    }

    @RequestMapping(path = "/organizations", method = GET)
    public Page<ResourceWithPermissions<Organization>> getPage(Pageable pageRequest, Set<FieldFilter> filters) {
        return orgService.getPage(pageRequest, filters).map(permissionsResolver::enrichWithPermissions);
    }

    @RequestMapping(path = "/organizations/{id}", method = GET)
    @Secured(VIEW_ORGANIZATION_MANAGEMENT)
    public Organization getOrganization(@PathVariable long id) {
        return orgService.getOrganization(id);
    }

    @RequestMapping(path = "/organizations", method = POST)
    @Secured(EDIT_ORGANIZATIONS)
    public Organization create(@RequestBody @Valid OrganizationCreateRequest org) {
        return orgService.create(org);
    }

    @RequestMapping(path = "/organizations/{id}", method = PUT)
    @Secured(EDIT_ORGANIZATIONS)
    public Organization update(@PathVariable long id, @RequestBody @Valid OrganizationUpdateRequest org) {
        return orgService.update(id, org);
    }

    @RequestMapping(path = "/organizations/{id}", method = DELETE)
    @Secured(EDIT_ORGANIZATIONS)
    public void delete(@PathVariable long id) {
        orgService.delete(id);
    }

    @RequestMapping(path = "/organizations/name/uniqueness", method = GET)
    @Secured(EDIT_ORGANIZATIONS)
    public boolean checkNameUnique(String orgName) {
        return orgService.isNameAvailable(orgName);
    }

    @RequestMapping(path = "/organizations/cid/uniqueness", method = GET)
    @Secured(EDIT_ORGANIZATIONS)
    public boolean checkCustomerIdUnique(String customerId) {
        return !orgService.isCustomerIdTaken(customerId);
    }

    @RequestMapping(path = "/organizations/{id}/support-contacts", method = GET)
    public SupportContacts getSupportContacts(@PathVariable long id) {
        return orgService.getSupportContacts(id);
    }
}
