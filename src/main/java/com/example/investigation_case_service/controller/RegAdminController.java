package com.example.investigation_case_service.controller;

import com.example.investigation_case_service.dto.response.UserResponse;
import com.example.investigation_case_service.security.AppUserPrincipal;
import com.example.investigation_case_service.service.RegAdminService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/reg/admin")
@RequiredArgsConstructor
public class RegAdminController {
    private final RegAdminService regAdminService;

    @GetMapping("/users")
    @Operation(summary = "Получить пользователей своего региона")
    public List<UserResponse> getUsersByMyRegion(@AuthenticationPrincipal AppUserPrincipal user) {
        return regAdminService.getUsersByMyRegion(user.regionId());
    }
}
