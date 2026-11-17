package com.example.investigation_case_service.service;

import com.example.investigation_case_service.security.AppUserPrincipal;

public interface InvestigatorService {
    String changeRegion(Long toRegionId, AppUserPrincipal userPrincipal);
}
