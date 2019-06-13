package com.dizzion.portal.domain.dizzionteam.web;

import com.dizzion.portal.domain.dizzionteam.DizzionTeamService;
import com.dizzion.portal.domain.dizzionteam.dto.DizzionTeam;
import com.dizzion.portal.domain.dizzionteam.dto.DizzionTeamCreateUpdateRequest;
import com.dizzion.portal.domain.filter.FieldFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

import static com.dizzion.portal.domain.role.Permission.Constants.DIZZION_TEAMS_MANAGEMENT;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@Secured(DIZZION_TEAMS_MANAGEMENT)
public class DizzionTeamController {

    private final DizzionTeamService dizzionTeamService;

    public DizzionTeamController(DizzionTeamService dizzionTeamService) {
        this.dizzionTeamService = dizzionTeamService;
    }

    @RequestMapping(path = "/dizzion-teams", method = GET)
    public Page<DizzionTeam> getPage(Pageable pageRequest, Set<FieldFilter> filters) {
        return dizzionTeamService.getPage(pageRequest, filters);
    }

    @RequestMapping(path = "/dizzion-teams/name/uniqueness", method = GET)
    public boolean checkNameUnique(String name) {
        return dizzionTeamService.isNameAvailable(name);
    }

    @RequestMapping(path = "/dizzion-teams", method = POST)
    public DizzionTeam create(@RequestBody @Valid DizzionTeamCreateUpdateRequest dizzionTeam) {
        return dizzionTeamService.create(dizzionTeam);
    }

    @RequestMapping(path = "/dizzion-teams/{id}", method = PUT)
    public DizzionTeam update(@PathVariable long id, @RequestBody @Valid DizzionTeamCreateUpdateRequest dizzionTeam) {
        return dizzionTeamService.update(id, dizzionTeam);
    }

    @RequestMapping(path = "/dizzion-teams/{id}", method = DELETE)
    public void delete(@PathVariable long id) {
        dizzionTeamService.delete(id);
    }
}
