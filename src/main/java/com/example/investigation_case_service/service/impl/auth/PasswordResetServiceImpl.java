package com.example.investigation_case_service.service.impl.auth;

import com.example.investigation_case_service.dto.request.ForgotPasswordRequest;
import com.example.investigation_case_service.dto.request.ResetPasswordRequest;
import com.example.investigation_case_service.dto.request.VerifyResetCodeRequest;
import com.example.investigation_case_service.exception.ApiException;
import com.example.investigation_case_service.model.users.PasswordResetToken;
import com.example.investigation_case_service.model.users.User;
import com.example.investigation_case_service.repository.PasswordResetTokenRepository;
import com.example.investigation_case_service.repository.RefreshTokenRepository;
import com.example.investigation_case_service.repository.UserRepository;
import com.example.investigation_case_service.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {
    private static final String BAD_CODE = "Код неверный или срок его действия истёк";

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.password-reset.code-ttl:5m}")
    private Duration codeTtl;

    @Value("${app.password-reset.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.password-reset.resend-interval:1m}")
    private Duration resendInterval;


    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = normalize(request == null ? null : request.getEmail());
        User user = userRepository.findByLogin(email).orElse(null);
        if (user == null || !user.isEnabled()) {
            log.info("Password reset requested for unknown or disabled account");
            return;
        }
        Instant now = Instant.now();
        PasswordResetToken last = tokenRepository.findTopByUser_LoginOrderByIdDesc(email).orElse(null);
        if (last != null && last.getCreatedAt().plus(resendInterval).isAfter(now)) {
            log.info("Password reset for user [{}] requested too often", user.getId());
            return;
        }
        tokenRepository.deleteAllByUserId(user.getId());

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setCodeHash(passwordEncoder.encode(code));
        token.setExpiresAt(now.plus(codeTtl));
        tokenRepository.save(token);

        try {
            emailService.sendResetCode(user.getLogin(), code, codeTtl);
            log.info("Password reset code sent to user [{}]", user.getId());
        } catch (MailException e) {
            log.error("Failed to send password reset code to user [{}]: {}", user.getId(), e.getMessage());
            tokenRepository.delete(token);
        }
    }

    @Override
    @Transactional(noRollbackFor = ApiException.class)
    public void verifyResetCode(VerifyResetCodeRequest request) {
        if (request == null) {
            throw ApiException.badRequest("Пустой запрос");
        }
        PasswordResetToken token = checkedToken(request.getEmail(), request.getCode());
        token.setVerified(true);
    }

    @Override
    @Transactional(noRollbackFor = ApiException.class)
    public void resetPassword(ResetPasswordRequest request) {
        if (request == null) {
            throw ApiException.badRequest("Пустой запрос");
        }
        String newPassword = request.getNewPassword();
        if (newPassword == null || newPassword.length() < 8 || newPassword.length() > 32) {
            throw ApiException.badRequest("Пароль должен быть от 8 до 32 символов");
        }
        PasswordResetToken token = checkedToken(request.getEmail(), request.getCode());
        User user = token.getUser();
        if (!user.isEnabled()) {
            throw ApiException.badRequest(BAD_CODE);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        refreshTokenRepository.revokeAllByUserId(user.getId());
        tokenRepository.delete(token);
        log.info("Password changed by reset code for user [{}]", user.getId());
    }


    private PasswordResetToken checkedToken(String rawEmail, String code) {
        PasswordResetToken token = tokenRepository.findTopByUser_LoginOrderByIdDesc(normalize(rawEmail))
                .orElseThrow(() -> ApiException.badRequest(BAD_CODE));
        if (token.getExpiresAt().isBefore(Instant.now()) || token.getAttempts() >= maxAttempts) {
            throw ApiException.badRequest(BAD_CODE);
        }
        if (code == null || !passwordEncoder.matches(code.trim(), token.getCodeHash())) {
            token.setAttempts(token.getAttempts() + 1);
            throw ApiException.badRequest(BAD_CODE);
        }
        return token;
    }

    private static String normalize(String email) {
        return email == null ? "" : email.trim();
    }
}
