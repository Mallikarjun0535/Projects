package com.dizzion.portal.domain.application;

import com.dizzion.portal.domain.application.dto.Application;
import com.dizzion.portal.domain.application.persistence.entity.ApplicationEntity;
import com.dizzion.portal.domain.common.ScopedEntityService;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.user.dto.User;
import com.dizzion.portal.domain.user.persistence.UserRepository;
import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Service
@Transactional
public class StarredApplicationService {
    private final ScopedEntityService scopedEntityService;
    private final UserRepository userRepo;
    private final AuthenticatedUserAccessor auth;

    public StarredApplicationService(ScopedEntityService scopedEntityService,
                                     UserRepository userRepo,
                                     AuthenticatedUserAccessor auth) {
        this.scopedEntityService = scopedEntityService;
        this.userRepo = userRepo;
        this.auth = auth;
    }

    @Transactional(readOnly = true)
    public Set<Application> getStarredApplications() {
        Set<Application> userStarredApps = getStarredApplicationForCurrentUser();
        return userStarredApps.isEmpty() || auth.getAuthenticatedUser().isScopeEnabled()
                ? getStarredApplicationForCurrentUserOrganization()
                : userStarredApps;
    }

    public void starApplicationForCurrentUser(long appId) {
        Set<ApplicationEntity> userStarredApps = loadCurrentUserEntity().getStarredApplications();
        if (userStarredApps.isEmpty()) {
            Set<ApplicationEntity> orgStarredApps = scopedEntityService
                    .getForRead(getCurrentUser().getOrganization().getId(), OrganizationEntity.class)
                    .getStarredApplications();
            userStarredApps.addAll(orgStarredApps);
        }
        userStarredApps.add(scopedEntityService.getForRead(appId, ApplicationEntity.class));
    }

    public void unstarApplicationForCurrentUser(long appId) {
        loadCurrentUserEntity().getStarredApplications()
                .remove(scopedEntityService.getForRead(appId, ApplicationEntity.class));
    }

    private Set<Application> getStarredApplicationForCurrentUserOrganization() {
        return scopedEntityService.getForRead(getCurrentUser().getOrganization().getId(), OrganizationEntity.class)
                .getStarredApplications().stream()
                .map(Application::starredFrom)
                .collect(toSet());
    }

    private Set<Application> getStarredApplicationForCurrentUser() {
        return loadCurrentUserEntity()
                .getStarredApplications().stream()
                .map(Application::starredFrom)
                .collect(toSet());
    }

    private UserEntity loadCurrentUserEntity() {
        // Can't use ScopedEntityService here, because if a user is in scope, they won't be able to load themselves with it
        return userRepo.findOne(getCurrentUser().getId());
    }

    private User getCurrentUser() {
        return auth.getAuthenticatedUser().getUser();
    }
}
