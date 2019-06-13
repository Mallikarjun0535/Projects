package com.dizzion.portal.config;

import com.dizzion.portal.security.auth.spring.JwtAuthenticationProvider;
import com.dizzion.portal.security.auth.spring.JwtGetParamAuthenticationFilter;
import com.dizzion.portal.security.auth.spring.JwtHeaderAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.CorsFilter;

import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static org.springframework.web.cors.CorsConfiguration.ALL;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationProvider jwtAuthenticationProvider;

    public SecurityConfig(JwtAuthenticationProvider jwtAuthenticationProvider) {
        this.jwtAuthenticationProvider = jwtAuthenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() throws Exception {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(jwtAuthenticationProvider);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(jwtGetParamAuthenticationFilter(), AnonymousAuthenticationFilter.class)
                .addFilterBefore(jwtHeaderAuthenticationFilter(), JwtGetParamAuthenticationFilter.class)
                .addFilterBefore(corsFilter(), JwtHeaderAuthenticationFilter.class)
                .sessionManagement().sessionCreationPolicy(STATELESS)
                .and()
                .exceptionHandling().authenticationEntryPoint(new Http403ForbiddenEntryPoint())
                .and()
                .csrf().disable();
    }

    private JwtHeaderAuthenticationFilter jwtHeaderAuthenticationFilter() throws Exception {
        return new JwtHeaderAuthenticationFilter(authenticationManager(), request ->
                Stream.of(
                        "/token",
                        "/two-factor-auth-type",
                        "/two-factor-auth-token",
                        "/registration/**",
                        "/maintenance-events/*/external-approval",
                        "/health",
                        "/cids/**")
                        .map(AntPathRequestMatcher::new)
                        .noneMatch(matcher -> matcher.matches(request))
        );
    }

    private JwtGetParamAuthenticationFilter jwtGetParamAuthenticationFilter() throws Exception {
        return new JwtGetParamAuthenticationFilter(authenticationManager(), request ->
                Stream.of("/maintenance-events/*/external-approval")
                        .map(AntPathRequestMatcher::new)
                        .anyMatch(matcher -> matcher.matches(request))
        );
    }

    private CorsFilter corsFilter() throws Exception {
        return new CorsFilter(request -> {
            CorsConfiguration corsConfiguration = new CorsConfiguration();
            corsConfiguration.addAllowedOrigin(ALL);
            corsConfiguration.addAllowedHeader(ALL);
            corsConfiguration.addAllowedMethod(ALL);
            return corsConfiguration;
        });
    }

    @EnableGlobalMethodSecurity(securedEnabled = true)
    public static class MethodSecurity extends GlobalMethodSecurityConfiguration {

        @Override
        public AccessDecisionManager accessDecisionManager() {
            RoleVoter emptyPrefixVoter = new RoleVoter();
            emptyPrefixVoter.setRolePrefix("");

            return new AffirmativeBased(asList(
                    emptyPrefixVoter,
                    new AuthenticatedVoter()
            ));
        }
    }
}
