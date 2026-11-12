package com.example.investigation_case_service.dto.request;

import com.example.investigation_case_service.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String fullName;
    private Role role;
    private Long regionId;
    private Boolean enabled;
}
