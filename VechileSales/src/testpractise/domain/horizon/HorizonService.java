package com.dizzion.portal.domain.horizon;

import com.dizzion.portal.domain.horizon.dto.HorizonAuthenticationRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Service
public class HorizonService {

    private static final String AUTH_TEMPLATE = "<broker version='10.0'><do-submit-authentication><screen><name>windows-password</name><params><param><name>username</name><values><value>${username}</value></values></param><param><name>domain</name><values><value>${domain}</value></values></param><param><name>password</name><values><value>${password}</value></values></param></params></screen></do-submit-authentication></broker>";
    private static final String TUNNEL_CONNECTION = "<broker version='10.0'><get-tunnel-connection><bypass-tunnel>true</bypass-tunnel><multi-connection-aware>true</multi-connection-aware></get-tunnel-connection></broker>";
    private static final String AUTH_SUCCESSFUL_COOKIE_NAME = "com.vmware.vdi.broker.location.id";
    private static final String CID_COOKIE = "CID=AgAAADtl+vsIXd66vnM3PGQyu08=";

    private final RestTemplate restTemplate;

    public HorizonService(CookielessRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Set<String> authenticateAndGetCookies(HorizonAuthenticationRequest authRequest) {
        String brokerUrl = brokerUrl(authRequest.getApplicationUrl());
        Set<String> appCookies = authenticate(brokerUrl, authRequest);
        setupTunnelConnection(brokerUrl, appCookies);
        return appCookies;
    }

    private String brokerUrl(String appUrl) {
        try {
            return "https://" + new URL(appUrl).getAuthority() + "/broker/xml";
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid application url: " + appUrl, e);
        }
    }

    private Set<String> authenticate(String brokerUrl, HorizonAuthenticationRequest loginRequest) {
        String auth = AUTH_TEMPLATE
                .replace("${username}", loginRequest.getUsername())
                .replace("${password}", loginRequest.getPassword())
                .replace("${domain}", loginRequest.getDomain());

        Set<HttpCookie> cookies = restTemplate.postForEntity(brokerUrl, auth, String.class)
                .getHeaders().get("Set-Cookie").stream()
                .map(cookieStr -> HttpCookie.parse(cookieStr).get(0))
                .collect(toSet());

        if (cookies.stream().noneMatch(cookie -> AUTH_SUCCESSFUL_COOKIE_NAME.equals(cookie.getName()))) {
            throw new BadCredentialsException("");
        } else {
            Set<String> httpOnlyCookies = cookies.stream()
                    .peek(cookie -> cookie.setHttpOnly(false))
                    .map(HttpCookie::toString)
                    .collect(toSet());
            httpOnlyCookies.add(CID_COOKIE);
            return httpOnlyCookies;
        }
    }

    private void setupTunnelConnection(String brokerUrl, Set<String> authCookies) {
        HttpHeaders headers = new HttpHeaders();
        authCookies.forEach(cookie -> headers.add("Cookie", cookie));

        HttpEntity<String> setupTunnelRequest = new HttpEntity<>(TUNNEL_CONNECTION, headers);
        restTemplate.postForEntity(brokerUrl, setupTunnelRequest, String.class);
    }
}
