package com.example.investigation_case_service.service.impl.auth;

import com.example.investigation_case_service.dto.request.LoginRequest;
import com.example.investigation_case_service.dto.request.RefreshRequest;
import com.example.investigation_case_service.dto.request.SignUpRequest;
import com.example.investigation_case_service.dto.response.RegionResponse;
import com.example.investigation_case_service.dto.response.TokenResponse;
import com.example.investigation_case_service.exception.ApiException;
import com.example.investigation_case_service.exception.AuthException;
import com.example.investigation_case_service.model.enums.reviews.ReviewStatus;
import com.example.investigation_case_service.model.enums.reviews.ReviewType;
import com.example.investigation_case_service.model.review.RegistrationReviewsDetails;
import com.example.investigation_case_service.model.review.Review;
import com.example.investigation_case_service.model.users.*;
import com.example.investigation_case_service.repository.*;
import com.example.investigation_case_service.repository.review.RegistrationReviewsRepository;
import com.example.investigation_case_service.repository.review.ReviewRepository;
import com.example.investigation_case_service.security.JwtService;
import com.example.investigation_case_service.service.auth.AuthService;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String BAD_CREDENTIALS = "Неверный логин или пароль";
    private static final String BAD_REFRESH = "Недействительный refresh-токен";

    private final UserRepository userRepository;
    private final RegistrationReviewsRepository registrationReviewsRepository;
    private final RegionRepository regionRepository;
    private final ReviewRepository reviewRepository;
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
        if (request == null || isBlank(request.getLogin()) || isBlank(request.getPassword())) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Логин и пароль обязательны");
        }
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
                log.info("User [{}] is locked until [{}]", user.getLogin(), user.getLockedUntil());
            }
            log.info("User [{}] entered a wrong password", user.getLogin());
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

    @Override
    @Transactional
    public String signUp(SignUpRequest request) {
        if (request == null) {
            throw ApiException.badRequest("Пустой запрос");
        }
        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        if (email.length() > 64 || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw ApiException.badRequest("Укажите корректный email не длиннее 64 символов");
        }
        String iin = request.getIin() == null ? "" : request.getIin().trim();
        if (!iin.matches("\\d{12}")) {
            throw ApiException.badRequest("ИИН должен состоять из 12 цифр");
        }
        if (userRepository.existsByLogin(email)) {
            log.info("User [{}] is already exists", email);
            throw ApiException.conflict("Пользователь с логином " + email + " уже существует");
        }
        if (userRepository.existsByIin(iin)) {
            throw ApiException.conflict("Пользователь с таким ИИН уже существует");
        }
        if (registrationReviewsRepository.existsByLoginAndReviewStatus(email, ReviewStatus.PENDING)
                || registrationReviewsRepository.existsByIinAndReviewStatus(iin, ReviewStatus.PENDING)) {
            throw ApiException.conflict("Заявка с таким email или ИИН уже ожидает рассмотрения");
        }
        String password = request.getPassword();
        if (password == null || password.length() < 6 || password.length() > 20) {
            throw ApiException.badRequest("Пароль должен быть от 6 до 20 символов");
        }
        if (isBlank(request.getName()) || isBlank(request.getSurname())) {
            throw ApiException.badRequest("Имя и фамилия обязательны");
        }
        if (request.getRegionId() == null) {
            throw ApiException.badRequest("regionId обязателен");
        }
        Region region = region(request.getRegionId());

        RegistrationReviewsDetails details = new RegistrationReviewsDetails();
        details.setRegion(region);
        details.setIin(iin);
        details.setLogin(email);
        details.setFirstName(request.getName().trim());
        details.setLastName(request.getSurname().trim());
        details.setFatherName(isBlank(request.getFathername()) ? null : request.getFathername().trim());
        details.setPasswordHash(passwordEncoder.encode(password));
        details.setReviewStatus(ReviewStatus.PENDING);
        details.setReview(createReview(ReviewType.REGISTRATION));
        registrationReviewsRepository.saveAndFlush(details);
        log.info("Registration request [{}] created for region [{}]", details.getId(), region.getId());
        return "Заявка на регистрацию отправлена региональному администратору";
    }

    private Review createReview(ReviewType reviewType) {
        Review review = new Review();
        review.setReviewStatus(ReviewStatus.PENDING);
        review.setReviewType(reviewType);
        return reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegionResponse> listRegions() {
        return regionRepository.findAll().stream()
                .map(reg -> new RegionResponse(reg.getId(), reg.getCode(), reg.getName()))
                .toList();
    }


    private Region region(Long id) {
        return regionRepository.findById(id).orElseThrow(() -> ApiException.badRequest("Регион " + id + " не найден"));
    }
}
