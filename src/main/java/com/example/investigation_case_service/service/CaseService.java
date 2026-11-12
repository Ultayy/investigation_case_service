package com.example.investigation_case_service.service;

import com.example.investigation_case_service.dto.request.CreateCaseRequest;
import com.example.investigation_case_service.dto.request.MemberRequest;
import com.example.investigation_case_service.dto.request.UpdateCaseRequest;
import com.example.investigation_case_service.dto.response.AuditEventResponse;
import com.example.investigation_case_service.dto.response.CaseFileResponse;
import com.example.investigation_case_service.dto.response.CaseResponse;
import com.example.investigation_case_service.dto.response.MemberResponse;
import com.example.investigation_case_service.security.AppUserPrincipal;

import java.util.List;

public interface CaseService {
    CaseResponse createCase(CreateCaseRequest request, AppUserPrincipal user);

    List<CaseResponse> listCases(AppUserPrincipal user);

    CaseResponse getCase(Long caseId, AppUserPrincipal user);

    CaseResponse updateCase(Long caseId, UpdateCaseRequest request, AppUserPrincipal user);

    MemberResponse putMember(Long caseId, Long userId, MemberRequest request, AppUserPrincipal user);

    void removeMember(Long caseId, Long userId, AppUserPrincipal user);

    List<CaseFileResponse> listFiles(Long caseId, AppUserPrincipal user);

    List<AuditEventResponse> history(Long caseId, AppUserPrincipal user);
}
