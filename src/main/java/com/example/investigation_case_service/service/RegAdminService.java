package com.example.investigation_case_service.service;

import com.example.investigation_case_service.dto.response.UserResponse;

import java.util.List;

public interface RegAdminService {
    List<UserResponse> getUsersByMyRegion(Long regionId);
}
