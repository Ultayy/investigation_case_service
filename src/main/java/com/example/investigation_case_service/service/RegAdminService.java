package com.example.investigation_case_service.service;

import com.example.investigation_case_service.dto.response.RegionChangeResponse;
import com.example.investigation_case_service.dto.response.RegistrationRequestResponse;
import com.example.investigation_case_service.dto.response.UserResponse;
import com.example.investigation_case_service.model.enums.reviews.ReviewStatus;
import com.example.investigation_case_service.security.AppUserPrincipal;
import org.springframework.data.domain.Page;

public interface RegAdminService {
    Page<UserResponse> getUsersByMyRegion(Long regionId, int page, int size);

    Page<RegistrationRequestResponse> getRegistrations(Long regionId, ReviewStatus status, int page, int size);

    Page<RegionChangeResponse> getRegionChanges(Long regionId, ReviewStatus status, int page, int size);

    String approveRegistration(Long requestId, AppUserPrincipal admin);

    String approveChangeRegion(Long requestId, AppUserPrincipal admin);

    RegistrationRequestResponse rejectRegistration(Long requestId, AppUserPrincipal admin);

    String rejectRegionChange(Long requestId, AppUserPrincipal admin);

}
