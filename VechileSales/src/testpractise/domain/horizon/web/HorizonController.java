package com.dizzion.portal.domain.horizon.web;

import com.dizzion.portal.domain.horizon.HorizonService;
import com.dizzion.portal.domain.horizon.dto.HorizonAuthenticationRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class HorizonController {

    private final HorizonService horizonService;

    public HorizonController(HorizonService horizonService) {
        this.horizonService = horizonService;
    }

    @RequestMapping(path = "/horizon-auth", method = POST)
    public Set<String> horizonAuthentication(@RequestBody @Valid HorizonAuthenticationRequest authRequest) {
        return horizonService.authenticateAndGetCookies(authRequest);
    }
}
