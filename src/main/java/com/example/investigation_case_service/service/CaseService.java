package com.example.investigation_case_service.service;

import com.example.investigation_case_service.dto.request.CreateCaseRequest;
import com.example.investigation_case_service.dto.request.MemberRequest;
import com.example.investigation_case_service.dto.request.UpdateCaseRequest;
import com.example.investigation_case_service.dto.response.AuditEventResponse;
import com.example.investigation_case_service.dto.response.CaseFileResponse;
import com.example.investigation_case_service.dto.response.CaseResponse;
import com.example.investigation_case_service.dto.response.MemberResponse;
import com.example.investigation_case_service.security.AppUserPrincipal;

import org.springframework.data.domain.Page;

import java.util.List;

public interface CaseService {
    CaseResponse createCase(CreateCaseRequest request, AppUserPrincipal user);

    Page<CaseResponse> listCases(AppUserPrincipal user, int page, int size);

    CaseResponse getCase(Long caseId, AppUserPrincipal user);

    CaseResponse updateCase(Long caseId, UpdateCaseRequest request, AppUserPrincipal user);

    MemberResponse putMember(Long caseId, Long userId, MemberRequest request, AppUserPrincipal user);

    void removeMember(Long caseId, Long userId, AppUserPrincipal user);

    List<CaseFileResponse> listFiles(Long caseId, AppUserPrincipal user);

    Page<AuditEventResponse> history(Long caseId, AppUserPrincipal user, int page, int size);
}
