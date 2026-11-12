package com.example.investigation_case_service.service;

import com.example.investigation_case_service.dto.request.LoginRequest;
import com.example.investigation_case_service.dto.request.RefreshRequest;
import com.example.investigation_case_service.dto.response.TokenResponse;

public interface AuthService {
    TokenResponse login(LoginRequest request);

    TokenResponse refresh(RefreshRequest request);
}
