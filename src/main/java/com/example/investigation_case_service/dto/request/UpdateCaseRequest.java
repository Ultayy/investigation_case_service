package com.example.investigation_case_service.dto.request;

import com.example.investigation_case_service.model.enums.cases.CaseStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCaseRequest {
    @Size(max = 255)
    private String title;

    private String description;

    private CaseStatus status;
}
