package com.example.investigation_case_service.controller;

import com.example.investigation_case_service.dto.request.CreateUserRequest;
import com.example.investigation_case_service.dto.request.UpdateUserRequest;
import com.example.investigation_case_service.dto.response.RegionResponse;
import com.example.investigation_case_service.dto.response.UserResponse;
import com.example.investigation_case_service.security.AppUserPrincipal;
import com.example.investigation_case_service.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminContreoller {
    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "Получить пользователей")
    public List<UserResponse> users() {
        return adminService.listUsers();
    }

    @GetMapping("/regions")
    @Operation(summary = "Получить регионы")
    public List<RegionResponse> regions() {
        return adminService.listRegions();
    }

    @PostMapping("/user/create")
    @Operation(summary = "Создать пользователя")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(request));
    }

    @PatchMapping("/users/{userId}")
    @Operation(summary = "Обновление пользователя")
    public UserResponse updateUser(@PathVariable("userId") Long userId,
                              @RequestBody UpdateUserRequest request,
                              @AuthenticationPrincipal AppUserPrincipal user) {
        return adminService.updateUser(userId, request, user.id());
    }
}
