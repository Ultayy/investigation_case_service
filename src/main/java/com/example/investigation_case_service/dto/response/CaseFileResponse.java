package com.example.investigation_case_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaseFileResponse {
    private Long id;
    private String name;
    private String caseNumber;
}
