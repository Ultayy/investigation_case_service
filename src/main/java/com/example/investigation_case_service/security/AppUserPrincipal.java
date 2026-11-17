package com.example.investigation_case_service.security;

import com.example.investigation_case_service.model.enums.users.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

public record AppUserPrincipal(Long id, String login, Role role, Long regionId) {
    public Collection<GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
