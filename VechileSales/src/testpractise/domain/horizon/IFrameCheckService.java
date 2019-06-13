package com.dizzion.portal.domain.horizon;

import com.google.common.net.InternetDomainName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import static org.springframework.http.HttpMethod.GET;

@Service
public class IFrameCheckService {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";

    private final Optional<String> frontendUrlPrivateDomain;
    private final RestTemplate restTemplate;

    public IFrameCheckService(CookielessRestTemplate restTemplate,
                              @Value("${dizzion.frontend.url}") String frontendUrl) {
        this.restTemplate = restTemplate;
        this.frontendUrlPrivateDomain = privateDomain(frontendUrl);
    }

    public boolean isEmbeddable(String url) {
        try {
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.set("User-Agent", USER_AGENT);
            HttpEntity<String> entity = new HttpEntity<>(requestHeaders);

            HttpHeaders responseHeaders = restTemplate.exchange(url, GET, entity, String.class).getHeaders();

            return isEmbeddable(url, responseHeaders);
        } catch (HttpServerErrorException ex) {
            return isEmbeddable(url, ex.getResponseHeaders());
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isEmbeddable(String url, HttpHeaders responseHeaders) {
        return !hasCSP(responseHeaders) && allowedByXFrameOptions(url, getXFrameOptionsHeader(responseHeaders));
    }

    private Optional<String> getXFrameOptionsHeader(HttpHeaders responseHeaders) {
        return Optional.ofNullable(responseHeaders.get("X-Frame-Options"))
                .map(xFrameOptionsList -> xFrameOptionsList.get(0));
    }

    private boolean hasCSP(HttpHeaders responseHeaders) {
        return Optional.ofNullable(responseHeaders.get("Content-Security-Policy")).isPresent();
    }

    private boolean allowedByXFrameOptions(String url, Optional<String> xFrameOptions) {
        return !xFrameOptions.isPresent()
                || (xFrameOptions.get().equalsIgnoreCase("SAMEORIGIN") && sameDomain(url));
    }

    private boolean sameDomain(String url) {
        try {
            String host = new URL(url).getHost();
            Optional<String> domain = privateDomain(host);
            return domain.isPresent() && domain.equals(frontendUrlPrivateDomain);
        } catch (MalformedURLException e) {
            return false;
        }
    }

    private Optional<String> privateDomain(String url) {
        try {
            return Optional.of(InternetDomainName.from(new URL(url).getHost()).topPrivateDomain().toString());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
