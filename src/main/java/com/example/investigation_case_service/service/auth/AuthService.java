package com.example.investigation_case_service.service.auth;

import com.example.investigation_case_service.dto.request.LoginRequest;
import com.example.investigation_case_service.dto.request.RefreshRequest;
import com.example.investigation_case_service.dto.request.SignUpRequest;
import com.example.investigation_case_service.dto.response.RegionResponse;
import com.example.investigation_case_service.dto.response.TokenResponse;

import java.util.List;

public interface AuthService {
    TokenResponse login(LoginRequest request);
    String signUp(SignUpRequest request);

    List<RegionResponse> listRegions();

    TokenResponse refresh(RefreshRequest request);
}
