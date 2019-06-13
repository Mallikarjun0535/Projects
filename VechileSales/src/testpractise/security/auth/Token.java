package com.dizzion.portal.security.auth;

import com.dizzion.portal.domain.organization.dto.Organization;
import com.dizzion.portal.domain.organization.dto.Organization.Feature;
import com.dizzion.portal.domain.organization.dto.Organization.OrganizationType;
import com.dizzion.portal.domain.role.Permission;
import com.dizzion.portal.domain.role.dto.Role;
import com.dizzion.portal.domain.user.dto.User;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.Builder;
import lombok.Value;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;

@Value
@Builder
public class Token {
    long userId;
    String email;
    String role;
    long organizationId;
    String organizationCid;
    String organizationName;
    OrganizationType organizationType;
    Set<OrganizationType> availableOrgTypes;
    Set<String> permissions;
    Token scope;
    boolean memberOfDizzionTeams;
    boolean twoFactorAuth;

    public static Token from(User user) {
        return prebuild(user, user.getOrganization(), user.getRole()).build();
    }

    public static Token from(User user, Organization org, Role role) {
        return prebuild(user, user.getOrganization(), user.getRole())
                .scope(prebuild(user, org, role).build())
                .build();
    }

    public static Token from(Map claims) {
        Map scopeClaims = (Map) claims.get("scope");
        return Token.builder()
                .userId(extractLong(claims, "sub"))
                .role((String) claims.get("role"))
                .email((String) claims.get("email"))
                .permissions(extractSet(claims, "permissions", p -> p))
                .organizationId(extractLong(claims, "organizationId"))
                .organizationCid((String) claims.get("organizationCid"))
                .organizationName((String) claims.get("organizationName"))
                .organizationType(OrganizationType.valueOf((String) claims.get("organizationType")))
                .availableOrgTypes(extractSet(claims, "availableOrgTypes", OrganizationType::valueOf))
                .memberOfDizzionTeams(extractBoolean(claims, "memberOfDizzionTeams"))
                .twoFactorAuth(extractBoolean(claims, "twoFactorAuth"))
                .scope(scopeClaims == null ? null : from(scopeClaims))
                .build();
    }

    public ImmutableMap<String, Object> asMap() {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.<String, Object>builder()
                .put("sub", userId)
                .put("email", email)
                .put("role", role)
                .put("permissions", permissions)
                .put("organizationType", organizationType)
                .put("availableOrgTypes", organizationType.getAvailableTypes())
                .put("organizationId", organizationId)
                .put("organizationCid", organizationCid)
                .put("organizationName", organizationName)
                .put("memberOfDizzionTeams", memberOfDizzionTeams)
                .put("twoFactorAuth", twoFactorAuth);
        if (scope != null) {
            builder.put("scope", scope.asMap());
        }
        return builder.build();
    }

    private static TokenBuilder prebuild(User user, Organization org, Role role) {
        Set<String> permissions = ImmutableSet.<String>builder()
                .addAll(role.getPermissions().stream().map(Permission::toString).collect(toSet()))
                .addAll(org.getFeatures().stream().map(Feature::asPermission).collect(toSet()))
                .build();

        return Token.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(role.getName())
                .permissions(permissions)
                .organizationId(org.getId())
                .organizationCid(org.getCustomerId())
                .organizationName(org.getName())
                .organizationType(org.getType())
                .availableOrgTypes(org.getType().getAvailableTypes())
                .memberOfDizzionTeams(user.isMemberOfDizzionTeams())
                .twoFactorAuth(org.isTwoFactorAuth());
    }

    private static long extractLong(Map claims, String name) {
        return ((Number) claims.get(name)).longValue();
    }

    private static boolean extractBoolean(Map claims, String name) {
        return (Boolean) claims.get(name);
    }

    private static <T> Set<T> extractSet(Map claims, String name, Function<String, T> typeConverter) {
        List extractedList = (ArrayList) Optional.ofNullable(claims.get(name))
                .orElseThrow(() -> new BadCredentialsException("Invalid token"));
        Set<T> items = new HashSet<>();
        for (Object item : extractedList) {
            items.add(typeConverter.apply((String) item));
        }
        return items;
    }
}
