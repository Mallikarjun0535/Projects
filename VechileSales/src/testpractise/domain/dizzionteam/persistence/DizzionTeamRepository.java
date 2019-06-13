package com.dizzion.portal.domain.dizzionteam.persistence;

import com.dizzion.portal.domain.common.persistence.CrudAndSpecificationExecutorRepository;
import com.dizzion.portal.domain.dizzionteam.persistence.entity.DizzionTeamEntity;

import java.util.Set;

public interface DizzionTeamRepository extends CrudAndSpecificationExecutorRepository<DizzionTeamEntity> {
    DizzionTeamEntity findByName(String name);

    Set<DizzionTeamEntity> findAllByUsers_Id(long id);
}
