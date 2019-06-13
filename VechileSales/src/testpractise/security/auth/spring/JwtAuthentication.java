package com.dizzion.portal.security.auth.spring;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import static java.util.Collections.emptyList;

public class JwtAuthentication extends AbstractAuthenticationToken {

    private final String token;
    private final boolean noscope;

    public JwtAuthentication(String token, boolean noscope) {
        super(emptyList());
        this.token = token;
        this.noscope = noscope;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    public String getToken() {
        return token;
    }

    public boolean isNoscope() {
        return noscope;
    }
}
