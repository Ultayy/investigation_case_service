package com.example.investigation_case_service.dto.request;

import com.example.investigation_case_service.model.enums.users.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank
    private String login;

    @NotBlank
    @Size(min = 8, max = 32)
    private String password;

    @NotBlank
    private String fullName;

    @NotNull
    private Role role;

    @NotNull
    private Long regionId;
}
