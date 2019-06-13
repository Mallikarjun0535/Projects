package com.dizzion.portal.domain.helpdesk;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ConnectWiseSystemService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ConnectWiseSystemService(@Value("${connectwise.service.base-url}") String baseUrl,
                                    @Qualifier("connectWiseRestTemplate") RestTemplate restTemplate) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplate;
    }

    public boolean isAvailable() {
        ResponseEntity<Object> response = restTemplate.getForEntity(baseUrl + "system/info", Object.class);
        return response.getStatusCode() == HttpStatus.OK;
    }
}
