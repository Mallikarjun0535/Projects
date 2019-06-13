package com.dizzion.portal.domain.helpdesk;

import com.dizzion.portal.domain.helpdesk.dto.OrganizationTemperatureChange;
import com.dizzion.portal.domain.helpdesk.dto.OrganizationTemperatureChange.Temperature;
import com.dizzion.portal.domain.helpdesk.dto.OrganizationTemperatureChangeRequest;
import com.dizzion.portal.domain.helpdesk.persistence.OrganizationTemperatureChangeRepository;
import com.dizzion.portal.domain.helpdesk.persistence.entity.OrganizationTemperatureChangeEntity;
import com.dizzion.portal.domain.organization.persistence.OrganizationRepository;
import com.dizzion.portal.domain.user.persistence.UserRepository;
import com.dizzion.portal.security.auth.AuthenticatedUserAccessor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static java.time.ZonedDateTime.now;
import static java.util.stream.Collectors.toMap;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
@Transactional
public class OrganizationTemperatureService {

    private final OrganizationTemperatureChangeRepository changeRepo;
    private final OrganizationRepository orgRepo;
    private final UserRepository userRepo;
    private final AuthenticatedUserAccessor auth;

    public OrganizationTemperatureService(OrganizationTemperatureChangeRepository changeRepo,
                                          OrganizationRepository orgRepo,
                                          UserRepository userRepo,
                                          AuthenticatedUserAccessor auth) {
        this.changeRepo = changeRepo;
        this.orgRepo = orgRepo;
        this.userRepo = userRepo;
        this.auth = auth;
    }

    @Transactional(readOnly = true)
    public Map<Long, Temperature> getCurrentTemperatureOfEachOrganization(List<Long> organizationIds) {
        return changeRepo.findLastChangeInEachOrganization(organizationIds).stream()
                .collect(toMap(
                        change -> change.getOrganization().getId(),
                        OrganizationTemperatureChangeEntity::getTemperature));
    }

    @Transactional(readOnly = true)
    public List<OrganizationTemperatureChange> getTemperatureHistory(long organizationId) {
        Pageable last20 = new PageRequest(0, 20, new Sort(DESC, "timestamp"));
        return changeRepo.findByOrganizationId(organizationId, last20)
                .map(OrganizationTemperatureChange::from)
                .getContent();
    }

    public OrganizationTemperatureChange changeTemperature(long id, OrganizationTemperatureChangeRequest request) {
        OrganizationTemperatureChangeEntity entity = OrganizationTemperatureChangeEntity.builder()
                .organization(orgRepo.findOne(id))
                .temperature(request.getTemperature())
                .comment(request.getComment())
                .timestamp(now())
                .user(userRepo.findOne(auth.getAuthenticatedUser().getUser().getId()))
                .build();
        return OrganizationTemperatureChange.from(changeRepo.save(entity));
    }
}
