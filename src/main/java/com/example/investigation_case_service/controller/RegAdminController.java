package com.example.investigation_case_service.controller;

import com.example.investigation_case_service.dto.response.RegionChangeResponse;
import com.example.investigation_case_service.dto.response.RegistrationRequestResponse;
import com.example.investigation_case_service.dto.response.UserResponse;
import com.example.investigation_case_service.model.enums.reviews.ReviewStatus;
import com.example.investigation_case_service.security.AppUserPrincipal;
import com.example.investigation_case_service.service.RegAdminService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reg/admin")
@RequiredArgsConstructor
public class RegAdminController {
    private final RegAdminService regAdminService;

    @GetMapping("/users")
    @Operation(summary = "Получить пользователей своего региона")
    public Page<UserResponse> getUsersByMyRegion(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal AppUserPrincipal regAdmin) {
        return regAdminService.getUsersByMyRegion(regAdmin.regionId(), page, size);
    }

    @GetMapping("/requests")
    @Operation(summary = "Заявки на регистрацию своего региона (по умолчанию нерассмотренные)")
    public Page<RegistrationRequestResponse> getRegistrations(
            @RequestParam(name = "status", defaultValue = "PENDING") ReviewStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal AppUserPrincipal regAdmin) {
        return regAdminService.getRegistrations(regAdmin.regionId(), status, page, size);
    }

    @PostMapping("/approve-registration/{id}")
    @Operation(summary = "Одобрить заявку на регистрацию: создаётся пользователь")
    public String approveRegistration(@PathVariable("id") Long requestId,
                                            @AuthenticationPrincipal AppUserPrincipal regAdmin) {
        return regAdminService.approveRegistration(requestId, regAdmin);
    }

    @PostMapping("/reject-registration/{id}")
    @Operation(summary = "Отклонить заявку")
    public RegistrationRequestResponse rejectRegistration(
            @PathVariable("id") Long requestId,
            @AuthenticationPrincipal AppUserPrincipal regAdmin) {
        return regAdminService.rejectRegistration(requestId, regAdmin);
    }

    @GetMapping("/change-region-requests")
    @Operation(summary = "Заявки на смену региона, где мой регион старый или новый (по умолчанию нерассмотренные)")
    public Page<RegionChangeResponse> getRegionChanges(
            @RequestParam(name = "status", defaultValue = "PENDING") ReviewStatus status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal AppUserPrincipal regAdmin) {
        return regAdminService.getRegionChanges(regAdmin.regionId(), status, page, size);
    }

    @PostMapping("/approve-change-region/{id}")
    @Operation(summary = "Одобрить заявку на смену региона")
    public String approveChangeRegion(@PathVariable("id") Long requestId,
                                      @AuthenticationPrincipal AppUserPrincipal regAdmin) {
        return regAdminService.approveChangeRegion(requestId, regAdmin);
    }

    @PostMapping("/reject-change-region/{id}")
    @Operation(summary = "Отклонить заявку на смену региона")
    public String rejectChangeRegion(
            @PathVariable("id") Long requestId,
            @AuthenticationPrincipal AppUserPrincipal regAdmin) {
        return regAdminService.rejectRegionChange(requestId, regAdmin);
    }
}
