package com.dizzion.portal.security.auth.spring;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class JwtHeaderAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String NOSCOPE_PARAM_NAME = "noscope";

    public JwtHeaderAuthenticationFilter(AuthenticationManager authMan, RequestMatcher requestMatcher) {
        super(requestMatcher);
        setAuthenticationManager(authMan);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        return extractJwtToken(request)
                .map(token -> {
                    boolean noscope = Optional.ofNullable(request.getParameter(NOSCOPE_PARAM_NAME))
                            .map(Boolean::valueOf)
                            .orElse(false);
                    return getAuthenticationManager().authenticate(new JwtAuthentication(token, noscope));
                })
                .orElseThrow(() -> new AuthenticationCredentialsNotFoundException("Token not found"));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authResult)
            throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }

    private static Optional<String> extractJwtToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION))
                .filter(header -> header.startsWith(BEARER_PREFIX))
                .map(header -> header.substring(BEARER_PREFIX.length()));
    }
}