package com.example.investigation_case_service.controller;

import com.example.investigation_case_service.dto.request.CreateCaseRequest;
import com.example.investigation_case_service.dto.request.MemberRequest;
import com.example.investigation_case_service.dto.request.UpdateCaseRequest;
import com.example.investigation_case_service.dto.response.AuditEventResponse;
import com.example.investigation_case_service.dto.response.CaseFileResponse;
import com.example.investigation_case_service.dto.response.CaseResponse;
import com.example.investigation_case_service.dto.response.MemberResponse;
import com.example.investigation_case_service.security.AppUserPrincipal;
import com.example.investigation_case_service.service.CaseService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cases")
@RequiredArgsConstructor
public class CaseController {
    private final CaseService caseService;

    @PostMapping("/create")
    @Operation(summary = "Создать дело")
    @PreAuthorize("hasRole('INVESTIGATOR')")
    public ResponseEntity<CaseResponse> create(@Valid @RequestBody CreateCaseRequest request,
                                               @AuthenticationPrincipal AppUserPrincipal userPrincipal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(caseService.createCase(request, userPrincipal));
    }

    @Operation(summary = "Посмотреть дела")
    @GetMapping("/getAll")
    public List<CaseResponse> list(@AuthenticationPrincipal AppUserPrincipal userPrincipal) {
        return caseService.listCases(userPrincipal);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить дело")
    public CaseResponse get(@PathVariable("id") Long caseId,
                            @AuthenticationPrincipal AppUserPrincipal userPrincipal) {
        return caseService.getCase(caseId, userPrincipal);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Изменить дело по id")
    public CaseResponse update(@PathVariable("id") Long caseId,
                               @RequestBody UpdateCaseRequest request,
                               @AuthenticationPrincipal AppUserPrincipal userPrincipal) {
        return caseService.updateCase(caseId, request, userPrincipal);
    }

    @PutMapping("/{id}/members/{userId}")
    @Operation(summary = "Выдать или изменить доступ ")
    public MemberResponse putMember(@PathVariable("id") Long caseId,
                                    @PathVariable("userId") Long userId,
                                    @RequestBody MemberRequest request,
                                    @AuthenticationPrincipal AppUserPrincipal userPrincipal) {
        return caseService.putMember(caseId, userId, request, userPrincipal);
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "отозвать доступ у участника")
    public ResponseEntity<Void> removeMember(@PathVariable("id") Long caseId,
                                             @PathVariable("userId") Long userId,
                                             @AuthenticationPrincipal AppUserPrincipal userPrincipal) {
        caseService.removeMember(caseId, userId, userPrincipal);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/files")
    @Operation(summary = "получить файлы по делу")
    public List<CaseFileResponse> files(@PathVariable("id") Long caseId,
                                        @AuthenticationPrincipal AppUserPrincipal userPrincipal) {
        return caseService.listFiles(caseId, userPrincipal);
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Получить историю дело")
    public List<AuditEventResponse> history(@PathVariable("id") Long caseId,
                                            @AuthenticationPrincipal AppUserPrincipal userPrincipal) {
        return caseService.history(caseId, userPrincipal);
    }
}
