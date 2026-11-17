package com.example.investigation_case_service.controller;

import com.example.investigation_case_service.security.AppUserPrincipal;
import com.example.investigation_case_service.service.InvestigatorService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/investigator")
@RequiredArgsConstructor
public class InvestigatorController {
    private final InvestigatorService investigatorService;

    @PostMapping("/region/{toId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Подать заявку на смену региона (из своего текущего региона в указанный)")
    public String changeRegion(@PathVariable("toId") Long toRegionId,
                               @AuthenticationPrincipal AppUserPrincipal investigator) {
        return investigatorService.changeRegion(toRegionId, investigator);
    }
}
