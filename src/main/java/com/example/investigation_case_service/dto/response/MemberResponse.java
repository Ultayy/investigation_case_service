package com.example.investigation_case_service.dto.response;

import com.example.investigation_case_service.model.enums.MemberType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private Long caseId;
    private Long userId;
    private String login;
    private MemberType memberType;
    private boolean canView;
    private boolean canEdit;
    private List<Long> fileIds;
    private Instant grantedAt;
    private Instant updatedAt;
}
