package com.dizzion.portal.domain.helpdesk.web;

import com.dizzion.portal.domain.helpdesk.OrganizationTemperatureService;
import com.dizzion.portal.domain.helpdesk.dto.OrganizationTemperatureChange;
import com.dizzion.portal.domain.helpdesk.dto.OrganizationTemperatureChangeRequest;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
public class OrganizationTemperatureChangeController {

    private final OrganizationTemperatureService temperatureService;

    public OrganizationTemperatureChangeController(OrganizationTemperatureService temperatureService) {
        this.temperatureService = temperatureService;
    }

    @GetMapping("/organizations/{id}/temperature-history")
    public List<OrganizationTemperatureChange> getLastChanges(@PathVariable long id) {
        return temperatureService.getTemperatureHistory(id);
    }

    @PostMapping("/organizations/{id}/temperature-history")
    public OrganizationTemperatureChange create(@PathVariable long id, @RequestBody @Valid OrganizationTemperatureChangeRequest request) {
        return temperatureService.changeTemperature(id, request);
    }
}
