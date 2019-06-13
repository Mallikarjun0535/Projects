package com.dizzion.portal.security.auth.persistence;

import com.dizzion.portal.domain.common.persistence.CrudAndSpecificationExecutorRepository;
import com.dizzion.portal.security.auth.persistence.entity.TwoFactorTokenEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TwoFactorTokenRepository extends CrudAndSpecificationExecutorRepository<TwoFactorTokenEntity> {

    @Modifying
    @Query(value = "INSERT INTO user_two_factor_token(user_id, token) VALUES (:userId, :token) " +
            "ON DUPLICATE KEY UPDATE token = :token", nativeQuery = true)
    void insertOrUpdate(long userId, int token);

    Optional<TwoFactorTokenEntity> findByUserId(long userId);
}
