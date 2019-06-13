package com.dizzion.portal.domain.role.persistence;

import com.dizzion.portal.domain.common.persistence.CrudAndSpecificationExecutorRepository;
import com.dizzion.portal.domain.role.persistence.entity.RoleEntity;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

public interface RoleRepository extends CrudAndSpecificationExecutorRepository<RoleEntity> {
    RoleEntity findByName(String name);

    @Query(value = "SELECT * FROM role selected_role " +
            "WHERE NOT EXISTS(" +
            "    SELECT permission.permission" +
            "    FROM permission" +
            "    WHERE role_id = selected_role.id AND permission.permission NOT IN (" +
            "      SELECT permission.permission" +
            "      FROM role, permission" +
            "      WHERE role.id = permission.role_id AND role.name = :roleName" +
            "    )" +
            ")",
            nativeQuery = true)
    Set<RoleEntity> findSubordinateRoles(String roleName);

}
