package com.example.investigation_case_service.service;

import com.example.investigation_case_service.dto.request.CreateUserRequest;
import com.example.investigation_case_service.dto.request.UpdateUserRequest;
import com.example.investigation_case_service.dto.response.RegionResponse;
import com.example.investigation_case_service.dto.response.UserResponse;

import org.springframework.data.domain.Page;

import java.util.List;

public interface AdminService {
    Page<UserResponse> listUsers(int page, int size);

    List<RegionResponse> listRegions();

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(Long userId, UpdateUserRequest request, Long myId);
}
