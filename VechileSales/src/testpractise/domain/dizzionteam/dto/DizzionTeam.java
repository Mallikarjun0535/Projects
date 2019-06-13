package com.dizzion.portal.domain.dizzionteam.dto;

import com.dizzion.portal.domain.dizzionteam.persistence.entity.DizzionTeamEntity;
import com.dizzion.portal.domain.organization.dto.Organization;
import com.dizzion.portal.domain.user.dto.User;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Value
@Builder
public class DizzionTeam {
    Long id;
    String name;
    List<User> users;
    List<Organization> organizations;

    public static DizzionTeam from(DizzionTeamEntity entity) {
        return DizzionTeam.builder()
                .id(entity.getId())
                .name(entity.getName())
                .organizations(entity.getOrganizations().stream()
                        .map(Organization::from)
                        .collect(toList()))
                .users(entity.getUsers().stream()
                        .map(User::from)
                        .collect(toList()))
                .build();
    }
}
