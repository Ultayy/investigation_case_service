package com.example.investigation_case_service.service.impl;

import com.example.investigation_case_service.dto.request.LoginRequest;
import com.example.investigation_case_service.dto.request.RefreshRequest;
import com.example.investigation_case_service.dto.response.TokenResponse;
import com.example.investigation_case_service.exception.AuthException;
import com.example.investigation_case_service.model.users.RefreshToken;
import com.example.investigation_case_service.model.users.User;
import com.example.investigation_case_service.repository.RefreshTokenRepository;
import com.example.investigation_case_service.repository.UserRepository;
import com.example.investigation_case_service.security.JwtService;
import com.example.investigation_case_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String BAD_CREDENTIALS = "Неверный логин или пароль";
    private static final String BAD_REFRESH = "Недействительный refresh-токен";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwt;

    @Value("${app.security.max-failed-attempts}")
    private int maxFailedAttempts;

    @Value("${app.security.lock-duration}")
    private Duration lockDuration;

    @Value("${app.jwt.refresh-ttl}")
    private Duration refreshTtl;

    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional(noRollbackFor = AuthException.class)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS));
        Instant now = Instant.now();

        if (!user.isEnabled()) {
            log.info("User [{}] is not enabled", user.getLogin());
            throw new AuthException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS);
        }
        if (user.getLockedUntil() != null) {
            if (user.getLockedUntil().isAfter(now)) {
                log.info("User [{}] is locked", user.getLogin());
                throw new AuthException(HttpStatus.LOCKED, "Аккаунт временно заблокирован");
            }
            user.setLockedUntil(null);
            user.setFailedAttempts(0);
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);
            if (attempts >= maxFailedAttempts) {
                user.setLockedUntil(now.plus(lockDuration));
                user.setFailedAttempts(0);
            }
            log.info("User [{}] is locked", user.getLogin());
            throw new AuthException(HttpStatus.UNAUTHORIZED, BAD_CREDENTIALS);
        }
        user.setFailedAttempts(0);
        return issueTokens(user);
    }

    @Override
    @Transactional(noRollbackFor = AuthException.class)
    public TokenResponse refresh(RefreshRequest request) {
        if (request == null || isBlank(request.getRefreshToken())) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "refreshToken обязателен");
        }
        RefreshToken old = refreshTokenRepository.findByTokenHash(sha256(request.getRefreshToken()))
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, BAD_REFRESH));
        log.info("Refresh Token [{}]", old.getTokenHash());
        if (old.isRevoked()) {
            refreshTokenRepository.revokeAllByUserId(old.getUser().getId());
            log.info("Refresh Token [{}] is revoked", old.getUser().getId());
            throw new AuthException(HttpStatus.UNAUTHORIZED, BAD_REFRESH);
        }
        if (old.getExpiresAt().isBefore(Instant.now()) || !old.getUser().isEnabled()) {
            log.info("Refresh Token [{}] is expired", old.getUser().getId());
            throw new AuthException(HttpStatus.UNAUTHORIZED, BAD_REFRESH);
        }
        old.setRevoked(true);
        return issueTokens(old.getUser());
    }

    private TokenResponse issueTokens(User user) {
        byte[] raw = new byte[32];
        random.nextBytes(raw);
        String refresh = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(sha256(refresh));
        rt.setExpiresAt(Instant.now().plus(refreshTtl));
        refreshTokenRepository.save(rt);

        return new TokenResponse(jwt.generateAccessToken(user), refresh, "Bearer", jwt.accessTtlSeconds());
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String sha256(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
