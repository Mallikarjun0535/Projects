package com.dizzion.portal.domain.application.web;

import com.dizzion.portal.domain.application.ApplicationGroupService;
import com.dizzion.portal.domain.application.dto.ApplicationGroup;
import com.dizzion.portal.domain.application.dto.ApplicationGroupCreateUpdateRequest;
import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.security.resource.ApplicationGroupPermissionsResolver;
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

import static com.dizzion.portal.domain.role.Permission.Constants.EDIT_APPLICATION_GROUPS;
import static com.dizzion.portal.domain.role.Permission.Constants.VIEW_APPLICATION_GROUPS;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
public class ApplicationGroupController {

    private final ApplicationGroupService appGroupService;
    private final ApplicationGroupPermissionsResolver permissionsResolver;

    public ApplicationGroupController(ApplicationGroupService appGroupService, ApplicationGroupPermissionsResolver permissionsResolver) {
        this.appGroupService = appGroupService;
        this.permissionsResolver = permissionsResolver;
    }

    @RequestMapping(path = "/application-groups", method = GET)
    @Secured(VIEW_APPLICATION_GROUPS)
    public Page<ResourceWithPermissions<ApplicationGroup>> getPage(Pageable pageRequest, Set<FieldFilter> filters) {
        return appGroupService.getPage(pageRequest, filters).map(permissionsResolver::enrichWithPermissions);
    }

    @RequestMapping(path = "/application-groups/name/uniqueness", method = GET)
    @Secured(EDIT_APPLICATION_GROUPS)
    public boolean checkNameUnique(String name) {
        return appGroupService.isNameAvailable(name);
    }

    @RequestMapping(path = "/application-groups", method = POST)
    @Secured(EDIT_APPLICATION_GROUPS)
    public ApplicationGroup create(@RequestBody @Valid ApplicationGroupCreateUpdateRequest appGroup) {
        return appGroupService.create(appGroup);
    }

    @RequestMapping(path = "/application-groups/{id}", method = PUT)
    @Secured(EDIT_APPLICATION_GROUPS)
    public ApplicationGroup update(@PathVariable long id, @RequestBody @Valid ApplicationGroupCreateUpdateRequest appGroup) {
        return appGroupService.update(id, appGroup);
    }

    @RequestMapping(path = "/application-groups/{id}", method = DELETE)
    @Secured(EDIT_APPLICATION_GROUPS)
    public void delete(@PathVariable long id) {
        appGroupService.delete(id);
    }
}
