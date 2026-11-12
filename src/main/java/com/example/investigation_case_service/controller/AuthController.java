package com.example.investigation_case_service.controller;

import com.example.investigation_case_service.dto.request.LoginRequest;
import com.example.investigation_case_service.dto.request.RefreshRequest;
import com.example.investigation_case_service.dto.response.TokenResponse;
import com.example.investigation_case_service.security.AppUserPrincipal;
import com.example.investigation_case_service.service.AuthService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        return auth.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshRequest request) {
        return auth.refresh(request);
    }
}
