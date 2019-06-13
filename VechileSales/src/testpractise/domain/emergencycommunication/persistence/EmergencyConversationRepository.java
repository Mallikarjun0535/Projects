package com.dizzion.portal.domain.emergencycommunication.persistence;

import com.dizzion.portal.domain.emergencycommunication.persistence.entity.EmergencyConversationEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EmergencyConversationRepository extends PagingAndSortingRepository<EmergencyConversationEntity, Long> {
}
