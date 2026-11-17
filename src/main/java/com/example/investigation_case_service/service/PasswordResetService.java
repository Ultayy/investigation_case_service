package com.example.investigation_case_service.service;

import com.example.investigation_case_service.dto.request.ForgotPasswordRequest;
import com.example.investigation_case_service.dto.request.ResetPasswordRequest;
import com.example.investigation_case_service.dto.request.VerifyResetCodeRequest;

public interface PasswordResetService {
    void forgotPassword(ForgotPasswordRequest request);

    void verifyResetCode(VerifyResetCodeRequest request);

    void resetPassword(ResetPasswordRequest request);
}
