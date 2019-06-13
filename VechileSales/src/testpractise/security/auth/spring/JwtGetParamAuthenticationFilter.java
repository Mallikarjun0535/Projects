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

public class JwtGetParamAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    public JwtGetParamAuthenticationFilter(AuthenticationManager authMan, RequestMatcher requestMatcher) {
        super(requestMatcher);
        setAuthenticationManager(authMan);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        return extractJwtToken(request)
                .map(token -> getAuthenticationManager().authenticate(new JwtAuthentication(token, false)))
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
        return Optional.ofNullable(request.getParameter("token"));
    }
}