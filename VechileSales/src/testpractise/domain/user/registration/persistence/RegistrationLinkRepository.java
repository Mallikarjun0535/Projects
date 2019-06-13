package com.dizzion.portal.domain.user.registration.persistence;

import com.dizzion.portal.domain.common.persistence.CrudAndSpecificationExecutorRepository;
import com.dizzion.portal.domain.user.registration.persistence.entity.RegistrationLinkEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RegistrationLinkRepository extends CrudAndSpecificationExecutorRepository<RegistrationLinkEntity> {

    @Modifying
    @Query(value = "INSERT INTO user_registration_link(user_id, link_secret_path) VALUES (:userId, :linkUniquePart) " +
            "ON DUPLICATE KEY UPDATE link_secret_path = :linkUniquePart", nativeQuery = true)
    void insertOrUpdate(long userId, String linkUniquePart);
}
