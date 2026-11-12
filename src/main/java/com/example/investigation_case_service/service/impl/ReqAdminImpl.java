package com.example.investigation_case_service.service.impl;

import com.example.investigation_case_service.dto.response.UserResponse;
import com.example.investigation_case_service.repository.UserRepository;
import com.example.investigation_case_service.service.RegAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReqAdminImpl implements RegAdminService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByMyRegion(Long regionId) {
        return userRepository.findAllByRegion(regionId).stream().map(UserResponse::from).toList();
    }

}
