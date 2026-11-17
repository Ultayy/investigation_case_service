package com.example.investigation_case_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpRequest {

    @NotEmpty
    @Email
    private String email;
    @NotEmpty
    @Size(min = 6, max = 20)
    private String password;
    @NotBlank
    @Size(min = 12, max = 12)
    private String iin;
    @NotBlank
    private String name;
    @NotBlank
    private String surname;
    @NotBlank
    private String fathername;
    @NotNull
    private Long regionId;
}
