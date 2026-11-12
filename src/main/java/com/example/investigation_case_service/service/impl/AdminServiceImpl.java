package com.example.investigation_case_service.service.impl;

import com.example.investigation_case_service.dto.request.CreateUserRequest;
import com.example.investigation_case_service.dto.request.UpdateUserRequest;
import com.example.investigation_case_service.dto.response.RegionResponse;
import com.example.investigation_case_service.dto.response.UserResponse;
import com.example.investigation_case_service.exception.ApiException;
import com.example.investigation_case_service.model.users.Region;
import com.example.investigation_case_service.model.users.User;
import com.example.investigation_case_service.repository.RefreshTokenRepository;
import com.example.investigation_case_service.repository.RegionRepository;
import com.example.investigation_case_service.repository.UserRepository;
import com.example.investigation_case_service.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RegionRepository regionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder encoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAllWithRegion().stream().map(UserResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegionResponse> listRegions() {
        return regionRepository.findAll().stream()
                .map(r -> new RegionResponse(r.getId(), r.getCode(), r.getName()))
                .toList();
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String login = request.getLogin().trim();
        if (userRepository.existsByLogin(login)) {
            log.info("User [{}] is already exists", login);
            throw ApiException.conflict("Пользователь с логином " + login + " уже существует");
        }
        User u = new User();
        u.setLogin(login);
        u.setFullName(request.getFullName().trim());
        u.setRole(request.getRole());
        u.setRegion(region(request.getRegionId()));
        u.setPasswordHash(encoder.encode(request.getPassword()));
        return UserResponse.from(userRepository.saveAndFlush(u));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request, Long myId) {
        if (request == null) {
            log.info("Request is null");
            throw ApiException.badRequest("Пустой запрос");
        }
        User u = userRepository.findWithRegionById(userId)
                .orElseThrow(() -> ApiException.notFound("Пользователь не найден"));

        if (request.getFullName() != null) {
            if (request.getFullName().isBlank()) {
                log.info("Full name is empty");
                throw ApiException.badRequest("fullName не может быть пустым");
            }
            u.setFullName(request.getFullName().trim());
        }
        if (request.getRole() != null) {
            u.setRole(request.getRole());
        }
        if (request.getRegionId() != null) {
            u.setRegion(region(request.getRegionId()));
        }
        if (request.getEnabled() != null) {
            if (!request.getEnabled() && u.getId().equals(myId)) {
                log.info("You cannot disable your own account");
                throw ApiException.badRequest("Нельзя отключить собственную учётную запись");
            }
            u.setEnabled(request.getEnabled());
            if (!request.getEnabled()) {
                refreshTokenRepository.revokeAllByUserId(u.getId());
            }
        }
        return UserResponse.from(u);
    }

    private Region region(Long id) {
        return regionRepository.findById(id).orElseThrow(() -> ApiException.badRequest("Регион " + id + " не найден"));
    }
}
