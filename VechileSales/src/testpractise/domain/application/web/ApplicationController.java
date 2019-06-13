package com.dizzion.portal.domain.application.web;

import com.dizzion.portal.domain.application.ApplicationService;
import com.dizzion.portal.domain.application.StarredApplicationService;
import com.dizzion.portal.domain.application.dto.Application;
import com.dizzion.portal.domain.application.dto.ApplicationCreateUpdateRequest;
import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.security.resource.ApplicationPermissionsResolver;
import com.dizzion.portal.security.resource.ResourceWithPermissions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;

import static com.dizzion.portal.domain.role.Permission.Constants.EDIT_APPLICATIONS;
import static com.dizzion.portal.domain.role.Permission.Constants.VIEW_APPLICATIONS;

@RestController
public class ApplicationController {

    private final ApplicationService appService;
    private final StarredApplicationService starredAppService;
    private final ApplicationPermissionsResolver permissionsResolver;

    public ApplicationController(ApplicationService appService, StarredApplicationService starredAppService, ApplicationPermissionsResolver permissionsResolver) {
        this.appService = appService;
        this.starredAppService = starredAppService;
        this.permissionsResolver = permissionsResolver;
    }

    @GetMapping("/applications")
    @Secured(VIEW_APPLICATIONS)
    public Page<ResourceWithPermissions<Application>> getApplicationsPage(Pageable pageRequest, Set<FieldFilter> filters) {
        return appService.getApplicationsPage(pageRequest, filters).map(permissionsResolver::enrichWithPermissions);
    }

    @GetMapping("/organizations/{orgId}/applications")
    @Secured(VIEW_APPLICATIONS)
    public Page<ResourceWithPermissions<Application>> getOrganizationApplicationsPage(@PathVariable long orgId,
                                                                                      Pageable pageRequest,
                                                                                      Set<FieldFilter> filters) {
        return appService.getOrganizationApplicationsPage(orgId, pageRequest, filters)
                .map(permissionsResolver::enrichWithPermissions);
    }

    @GetMapping("/applications/{id}")
    @Secured(VIEW_APPLICATIONS)
    public ResourceWithPermissions<Application> getApplication(@PathVariable long id) {
        return permissionsResolver.enrichWithPermissions(appService.getApplication(id));
    }

    @PostMapping("/applications")
    @Secured(EDIT_APPLICATIONS)
    public Application create(@RequestBody @Valid ApplicationCreateUpdateRequest app) {
        return appService.create(app);
    }

    @PutMapping("/applications/{id}")
    @Secured(EDIT_APPLICATIONS)
    public Application update(@PathVariable long id, @RequestBody @Valid ApplicationCreateUpdateRequest app) {
        return appService.update(id, app);
    }

    @DeleteMapping("/applications/{id}")
    @Secured(EDIT_APPLICATIONS)
    public void delete(@PathVariable long id) {
        appService.delete(id);
    }

    @GetMapping("/applications/starred")
    @Secured(VIEW_APPLICATIONS)
    public Set<Application> getStarredApplications() {
        return starredAppService.getStarredApplications();
    }

    @PostMapping("/applications/{id}/star")
    @Secured(VIEW_APPLICATIONS)
    public void starApplication(@PathVariable long id) {
        starredAppService.starApplicationForCurrentUser(id);
    }

    @DeleteMapping("/applications/{id}/star")
    @Secured(VIEW_APPLICATIONS)
    public void unstarApplication(@PathVariable long id) {
        starredAppService.unstarApplicationForCurrentUser(id);
    }
}
