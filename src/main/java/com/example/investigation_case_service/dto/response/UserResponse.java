package com.example.investigation_case_service.dto.response;

import com.example.investigation_case_service.model.enums.users.Role;
import com.example.investigation_case_service.model.users.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String login;
    private String fullName;
    private Role role;
    private Long regionId;
    private String regionCode;
    private boolean enabled;
    private Instant lockedUntil;
    private Instant createdAt;

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getLogin(), u.getFullName(), u.getRole(),
                u.getRegion().getId(), u.getRegion().getCode(), u.isEnabled(),
                u.getLockedUntil(), u.getCreatedAt());
    }
}
