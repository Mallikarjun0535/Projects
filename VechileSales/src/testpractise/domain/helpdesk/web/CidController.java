package com.dizzion.portal.domain.helpdesk.web;

import com.dizzion.portal.domain.helpdesk.ConnectWiseCompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

@RestController
public class CidController {

    private final ConnectWiseCompanyService connectWiseCompanyService;

    public CidController(ConnectWiseCompanyService connectWiseCompanyService) {
        this.connectWiseCompanyService = connectWiseCompanyService;
    }

    @RequestMapping(path = "/cids/{cid}", method = HEAD)
    public ResponseEntity checkCidExistsInHelpdesk(@PathVariable String cid) {
        return connectWiseCompanyService.getCompany(cid)
                .map(company -> ok().build())
                .orElseGet(() -> notFound().build());
    }
}
