package com.dizzion.portal.domain.dizzionteam;

import com.dizzion.portal.domain.common.ScopedEntityService;
import com.dizzion.portal.domain.dizzionteam.dto.DizzionTeam;
import com.dizzion.portal.domain.dizzionteam.dto.DizzionTeamCreateUpdateRequest;
import com.dizzion.portal.domain.dizzionteam.dto.OrganizationTicketStatistic;
import com.dizzion.portal.domain.dizzionteam.persistence.DizzionTeamRepository;
import com.dizzion.portal.domain.dizzionteam.persistence.entity.DizzionTeamEntity;
import com.dizzion.portal.domain.exception.EntityNotFoundException;
import com.dizzion.portal.domain.filter.FieldFilter;
import com.dizzion.portal.domain.filter.persistence.FilterSpecificationFactory;
import com.dizzion.portal.domain.helpdesk.ConnectWiseTicketingService;
import com.dizzion.portal.domain.helpdesk.OrganizationTemperatureService;
import com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket;
import com.dizzion.portal.domain.helpdesk.dto.OrganizationTemperatureChange.Temperature;
import com.dizzion.portal.domain.organization.dto.Organization;
import com.dizzion.portal.domain.organization.dto.Organization.OrganizationType;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Severity.HIGH;
import static com.dizzion.portal.domain.helpdesk.dto.ConnectWiseTicket.Severity.MEDIUM;
import static com.dizzion.portal.domain.helpdesk.dto.OrganizationTemperatureChange.Temperature.GREEN;
import static com.dizzion.portal.domain.organization.dto.Organization.Feature.VIEW_TICKETS;
import static com.dizzion.portal.domain.organization.dto.Organization.OrganizationType.DIZZION;
import static com.dizzion.portal.domain.organization.dto.Organization.OrganizationType.PORTAL_ADMIN;
import static io.jsonwebtoken.lang.Collections.isEmpty;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.*;

@Service
@Transactional
public class DizzionTeamService {

    private final FilterSpecificationFactory filterSpecFactory;
    private final DizzionTeamRepository dizzionTeamRepository;
    private final ScopedEntityService scopedEntityService;
    private final OrganizationTemperatureService temperatureService;
    private final AuthenticatedUserAccessor auth;
    private final ConnectWiseTicketingService connectWiseTicketingService;

    public DizzionTeamService(FilterSpecificationFactory filterSpecFactory,
                              DizzionTeamRepository dizzionTeamRepository,
                              ScopedEntityService scopedEntityService,
                              OrganizationTemperatureService temperatureService,
                              AuthenticatedUserAccessor auth,
                              ConnectWiseTicketingService connectWiseTicketingService) {
        this.filterSpecFactory = filterSpecFactory;
        this.dizzionTeamRepository = dizzionTeamRepository;
        this.scopedEntityService = scopedEntityService;
        this.temperatureService = temperatureService;
        this.auth = auth;
        this.connectWiseTicketingService = connectWiseTicketingService;
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Page<DizzionTeam> getPage(Pageable pageRequest, Set<FieldFilter> filters) {
        Specifications<DizzionTeamEntity> specification = filterSpecFactory.specificationFor(DizzionTeamEntity.class, filters);
        return dizzionTeamRepository.findAll(specification, pageRequest).map(DizzionTeam::from);
    }

    public Set<DizzionTeam> getDizzionTeamsByUser(long userId) {
        return dizzionTeamRepository.findAllByUsers_Id(userId).stream().map(DizzionTeam::from).collect(toSet());
    }

    @Transactional(readOnly = true)
    public boolean isNameAvailable(String name) {
        return dizzionTeamRepository.findByName(name) == null;
    }

    public DizzionTeam create(DizzionTeamCreateUpdateRequest dizzionTeam) {
        Set<OrganizationEntity> organizations =
                scopedEntityService.getForWrite(dizzionTeam.getOrganizationIds(), OrganizationEntity.class);
        Set<UserEntity> users =
                scopedEntityService.getForWrite(dizzionTeam.getUserIds(), UserEntity.class);

        checkUsersAreFromPortalAdminOrDizzionCompany(users);

        DizzionTeamEntity entity = DizzionTeamEntity.builder()
                .name(dizzionTeam.getName())
                .organizations(organizations)
                .users(users)
                .build();
        return DizzionTeam.from(dizzionTeamRepository.save(entity));
    }

    public DizzionTeam update(long id, DizzionTeamCreateUpdateRequest dizzionTeam) {
        DizzionTeamEntity existing = getDizzionTeam(id);

        Set<OrganizationEntity> organizations =
                scopedEntityService.getForWrite(dizzionTeam.getOrganizationIds(), OrganizationEntity.class);
        Set<UserEntity> users =
                scopedEntityService.getForWrite(dizzionTeam.getUserIds(), UserEntity.class);

        checkUsersAreFromPortalAdminOrDizzionCompany(users);

        existing.setName(dizzionTeam.getName());
        existing.setOrganizations(organizations);
        existing.setUsers(users);
        return DizzionTeam.from(dizzionTeamRepository.save(existing));
    }

    public void delete(long id) {
        dizzionTeamRepository.delete(id);
    }

    public Set<OrganizationTicketStatistic> getTicketStatistics() {
        Set<DizzionTeamEntity> dizzionTeams = dizzionTeamRepository.findAllByUsers_Id(auth.getAuthenticatedUser().getUser().getId());
        if (isEmpty(dizzionTeams)) {
            throw new EntityNotFoundException();
        }

        Set<String> cids = extractCids(dizzionTeams);
        if (cids.isEmpty()) {
            return emptySet();
        }

        ImmutableListMultimap<String, ConnectWiseTicket> ticketsPerCid = Multimaps.index(
                connectWiseTicketingService.getOpenTickets(cids),
                ticket -> ticket.getCompany().getIdentifier()
        );
        Map<OrganizationEntity, List<ConnectWiseTicket>> ticketsPerOrg = dizzionTeams.stream()
                .flatMap(dizzionTeam -> dizzionTeam.getOrganizations().stream())
                .filter(organizationEntity -> organizationEntity.getType() != PORTAL_ADMIN
                        && organizationEntity.isEnabled()
                        && organizationEntity.getFeatures().contains(VIEW_TICKETS))
                .collect(toMap(Function.identity(), org -> ticketsPerCid.get(org.getCustomerId())));


        List<Long> orgIds = ticketsPerOrg.keySet().stream().map(OrganizationEntity::getId).collect(toList());
        Map<Long, Temperature> temperatures = temperatureService.getCurrentTemperatureOfEachOrganization(orgIds);

        return calculateStatistics(ticketsPerOrg, temperatures);
    }

    private Set<OrganizationTicketStatistic> calculateStatistics(
            Map<OrganizationEntity, List<ConnectWiseTicket>> ticketsPerOrg,
            Map<Long, Temperature> organizationTemperatures) {
        return ticketsPerOrg.entrySet().stream().map(entry -> OrganizationTicketStatistic.builder()
                .organization(Organization.from(entry.getKey()))
                .openTickets(entry.getValue().size())
                .severityHighTickets((int) entry.getValue().stream()
                        .filter(ticket -> ticket.getSeverity().equals(HIGH))
                        .count())
                .severityMediumTickets((int) entry.getValue().stream()
                        .filter(ticket -> ticket.getSeverity().equals(MEDIUM))
                        .count())
                .temperature(organizationTemperatures.getOrDefault(entry.getKey().getId(), GREEN))
                .build()).collect(toSet());
    }

    private Set<String> extractCids(Set<DizzionTeamEntity> dizzionTeams) {
        return dizzionTeams.stream()
                .flatMap(dizzionTeam -> dizzionTeam.getOrganizations().stream())
                .filter(organizationEntity -> organizationEntity.getType() != PORTAL_ADMIN)
                .map(OrganizationEntity::getCustomerId)
                .collect(toSet());
    }

    private void checkUsersAreFromPortalAdminOrDizzionCompany(Set<UserEntity> users) {
        users.stream()
                .filter(user -> {
                    OrganizationType organizationType = user.getOrganization().getType();
                    return organizationType != DIZZION && organizationType != PORTAL_ADMIN;
                })
                .findAny()
                .ifPresent(user -> {
                    throw new IllegalArgumentException();
                });
    }

    private DizzionTeamEntity getDizzionTeam(long id) {
        DizzionTeamEntity dizzionTeam = dizzionTeamRepository.findOne(id);
        if (dizzionTeam == null) {
            throw new EntityNotFoundException();
        }

        return dizzionTeam;
    }
}
