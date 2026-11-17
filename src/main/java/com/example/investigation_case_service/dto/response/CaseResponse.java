package com.example.investigation_case_service.dto.response;

import com.example.investigation_case_service.model.cases.Case;
import com.example.investigation_case_service.model.enums.cases.CaseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaseResponse {
    private Long id;
    private String number;
    private String title;
    private String description;
    private CaseStatus status;
    private Long ownerId;
    private String ownerLogin;
    private Long regionId;
    private String regionCode;
    private Instant createdAt;
    private Instant updatedAt;

    public static CaseResponse from(Case c) {
        return new CaseResponse(c.getId(), c.getNumber(), c.getTitle(), c.getDescription(), c.getStatus(),
                c.getOwner().getId(), c.getOwner().getLogin(), c.getRegion().getId(), c.getRegion().getCode(),
                c.getCreatedAt(), c.getUpdatedAt());
    }
}
