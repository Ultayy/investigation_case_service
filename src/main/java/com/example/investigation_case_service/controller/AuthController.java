package com.example.investigation_case_service.controller;

import com.example.investigation_case_service.dto.request.ForgotPasswordRequest;
import com.example.investigation_case_service.dto.request.LoginRequest;
import com.example.investigation_case_service.dto.request.RefreshRequest;
import com.example.investigation_case_service.dto.request.ResetPasswordRequest;
import com.example.investigation_case_service.dto.request.SignUpRequest;
import com.example.investigation_case_service.dto.request.VerifyResetCodeRequest;
import com.example.investigation_case_service.dto.response.RegionResponse;
import com.example.investigation_case_service.dto.response.TokenResponse;
import com.example.investigation_case_service.service.PasswordResetService;
import com.example.investigation_case_service.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    @Operation(summary = "Аутентификация пользователя")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/signup")
    @Operation(summary = "Регистрация пользователя")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public String signup(@Valid @RequestBody SignUpRequest request) {
        return authService.signUp(request);
    }

    @GetMapping("/regions")
    public List<RegionResponse> regions() {
        return authService.listRegions();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновить access-токен по refresh-токену")
    public TokenResponse refresh(@RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Забыл пароль: отправить код на почту")
    public String forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
        return "Если такой email зарегистрирован, на него отправлен код";
    }

    @PostMapping("/verify-reset-code")
    @Operation(summary = "Проверить код из письма")
    public String verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest request) {
        passwordResetService.verifyResetCode(request);
        return "Код подтверждён";
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Задать новый пароль по коду из письма")
    public String resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return "Пароль изменён";
    }
}
