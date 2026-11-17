package com.example.investigation_case_service.model.cases;
import com.example.investigation_case_service.model.enums.cases.AuditEventType;
import com.example.investigation_case_service.model.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Table(name = "audit_events", indexes = @Index(columnList = "case_id, created_at"))
@Getter @Setter @NoArgsConstructor
public class AuditEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private AuditEventType eventType;

    @Column(name = "object_type", nullable = false, length = 32)
    private String objectType;

    @Column(name = "object_id", nullable = false)
    private String objectId;

    @Column(name = "old_value", columnDefinition = "text")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "text")
    private String newValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}