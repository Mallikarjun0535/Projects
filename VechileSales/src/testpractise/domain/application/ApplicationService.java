package com.dizzion.portal.domain.application;

import com.dizzion.portal.domain.application.dto.Application;
import com.dizzion.portal.domain.application.dto.ApplicationCreateUpdateRequest;
import com.dizzion.portal.domain.application.persistence.entity.ApplicationEntity;
import com.dizzion.portal.domain.application.persistence.entity.ApplicationGroupEntity;
import com.dizzion.portal.domain.common.AbstractCrudService;
import com.dizzion.portal.domain.common.ScopedEntityService;
import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Service
@Transactional
public class ApplicationService extends AbstractCrudService<ApplicationEntity> {

    private final AuthenticatedUserAccessor auth;
    private final StarredApplicationService starredAppService;

    public ApplicationService(AuthenticatedUserAccessor auth,
                              ScopedEntityService scopedEntityService,
                              StarredApplicationService starredAppService) {
        super(scopedEntityService);
        this.auth = auth;
        this.starredAppService = starredAppService;
    }

    @Transactional(readOnly = true)
    public Page<Application> getApplicationsPage(Pageable pageRequest, Set<FieldFilter> filters) {
        Set<Long> starredAppIds = starredAppService.getStarredApplications().stream()
                .map(Application::getId)
                .collect(toSet());
        return getEntitiesPage(pageRequest, filters).map(entity -> starredAppIds.contains(entity.getId())
                ? Application.starredFrom(entity)
                : Application.from(entity));
    }

    @Transactional(readOnly = true)
    public Page<Application> getOrganizationApplicationsPage(long orgId, Pageable pageRequest, Set<FieldFilter> filters) {
        String orgTenantPath = this.scopedEntityService.getForRead(orgId, OrganizationEntity.class).getTenantPath();
        return scopedEntityService.getPage(ApplicationEntity.class, pageRequest, filters, orgTenantPath)
                .map(Application::from);
    }

    @Transactional(readOnly = true)
    public Application getApplication(long id) {
        return Application.from(getForRead(id));
    }

    public Application create(ApplicationCreateUpdateRequest app) {
        Set<ApplicationGroupEntity> appGroups =
                scopedEntityService.getForWrite(app.getApplicationGroupIds(), ApplicationGroupEntity.class);

        ApplicationEntity entity = ApplicationEntity.builder()
                .name(app.getName())
                .description(app.getDescription())
                .url(app.getHorizon()
                        ? normaliseHorizonAppUrl(app.getUrl())
                        : addProtocol(app.getUrl(), "http"))
                .horizon(app.getHorizon())
                .owner(scopedEntityService.getForWrite(auth.getOrganization().getId(), OrganizationEntity.class))
                .applicationGroups(appGroups)
                .build();
        return Application.from(save(entity));
    }

    public Application update(long id, ApplicationCreateUpdateRequest app) {
        Set<ApplicationGroupEntity> appGroups =
                scopedEntityService.getForWrite(app.getApplicationGroupIds(), ApplicationGroupEntity.class);

        ApplicationEntity existing = getForWrite(id);
        existing.setName(app.getName());
        existing.setDescription(app.getDescription());
        existing.setUrl(app.getHorizon()
                ? normaliseHorizonAppUrl(app.getUrl())
                : addProtocol(app.getUrl(), "http"));
        existing.setHorizon(app.getHorizon());
        existing.setApplicationGroups(appGroups);
        return Application.from(save(existing));
    }

    private String addProtocol(String url, String protocol) {
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            url = protocol + "://" + url;
        }
        return url;
    }

    private String normaliseHorizonAppUrl(String originalUrl) {
        originalUrl = addProtocol(originalUrl, "https");
        try {
            URL url = new URL(originalUrl);
            String portString = url.getPort() == -1 ? "" : ":" + url.getPort();
            return url.getProtocol() + "://" + url.getHost() + portString;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
