package com.dizzion.portal.domain.emergencycommunication.persistence.entity;

import com.dizzion.portal.domain.emergencycommunication.dto.EmergencyConversation.ConversationType;
import com.dizzion.portal.domain.filter.NonFilterable;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationEntity;
import com.dizzion.portal.domain.organization.persistence.entity.OrganizationGroupEntity;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static javax.persistence.EnumType.STRING;

@Entity
@Table(name = "emergency_conversation")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmergencyConversationEntity {
    @Id
    @GeneratedValue
    private long id;
    @ManyToMany
    @JoinTable(name = "emergency_conversation_organization",
            joinColumns = @JoinColumn(name = "emergency_conversation_id"),
            inverseJoinColumns = @JoinColumn(name = "organization_id"))
    Set<OrganizationEntity> recipients;
    @ManyToMany
    @JoinTable(name = "emergency_conversation_organization_group",
            joinColumns = @JoinColumn(name = "emergency_conversation_id"),
            inverseJoinColumns = @JoinColumn(name = "organization_group_id"))
    Set<OrganizationGroupEntity> recipientGroups;
    @Enumerated(STRING)
    private ConversationType type;
    @NonFilterable
    @Type(type = "com.dizzion.portal.config.hibernate.json.JsonUserType")
    private Map<String, String> properties;
    private String name;
    private ZonedDateTime createdAt;
    private ZonedDateTime closedAt;
    private Long helpDeskTicketId;

    public boolean isClosed() {
        return closedAt != null;
    }

    public Optional<Long> getHelpDeskTicketId() {
        return Optional.ofNullable(helpDeskTicketId);
    }
}
