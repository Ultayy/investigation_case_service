package com.example.investigation_case_service.dto.response;

import com.example.investigation_case_service.model.cases.AuditEvent;
import com.example.investigation_case_service.model.enums.cases.AuditEventType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventResponse {
    private Long id;
    private AuditEventType eventType;
    private Long actorId;
    private String actorLogin;
    private String objectType;
    private String objectId;
    private String oldValue;
    private String newValue;
    private Instant createdAt;

    public static AuditEventResponse from(AuditEvent e) {
        return new AuditEventResponse(e.getId(), e.getEventType(), e.getActor().getId(), e.getActor().getLogin(),
                e.getObjectType(), e.getObjectId(), e.getOldValue(), e.getNewValue(), e.getCreatedAt());
    }
}
