package com.dizzion.portal.domain.emergencycommunication.persistence.entity;

import lombok.*;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "emergency_message")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmergencyMessageEntity {
    @Id
    @GeneratedValue
    private long id;
    private long emergencyConversationId;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;
    private ZonedDateTime createdAt;
    boolean internal;
}
