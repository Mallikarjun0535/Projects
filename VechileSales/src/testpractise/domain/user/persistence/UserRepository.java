package com.dizzion.portal.domain.user.persistence;

import com.dizzion.portal.domain.common.persistence.CrudAndSpecificationExecutorRepository;
import com.dizzion.portal.domain.user.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends CrudAndSpecificationExecutorRepository<UserEntity> {

    Optional<UserEntity> findByEmail(String email);

    Set<UserEntity> findByEmailIn(Collection<String> email);

    UserEntity findByEmailAndOrganizationTenantPathLike(String email, String tenantPath);

    Optional<UserEntity> findByRegistrationLink_linkSecretPath(String secretPath);

    Set<UserEntity> findByOrganizationIdIn(Collection<Long> id);

    Set<UserEntity> findByOrganizationIdInAndRoleNameIn(Collection<Long> id, Collection<String> roles);

    @Query(value = "SELECT count(*) FROM user WHERE organization_id = :organizationId AND notification_methods <> JSON_ARRAY()",
            nativeQuery = true)
    long countUsersWithEnabledNotifications(long organizationId);
}
