package com.dizzion.portal.domain.emergencycommunication.persistence;

import com.dizzion.portal.domain.common.persistence.CrudAndSpecificationExecutorRepository;
import com.dizzion.portal.domain.emergencycommunication.persistence.entity.EmergencyMessageEntity;

import java.util.List;

public interface EmergencyMessageRepository extends CrudAndSpecificationExecutorRepository<EmergencyMessageEntity> {
    List<EmergencyMessageEntity> findByEmergencyConversationIdOrderByCreatedAtAsc(long conversationId);

    List<EmergencyMessageEntity> findByEmergencyConversationIdAndInternalFalseOrderByCreatedAtAsc(long conversationId);
}
