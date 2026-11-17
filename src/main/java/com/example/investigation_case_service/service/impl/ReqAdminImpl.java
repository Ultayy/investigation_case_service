package com.example.investigation_case_service.service.impl;

import com.example.investigation_case_service.dto.response.RegionChangeResponse;
import com.example.investigation_case_service.dto.response.RegistrationRequestResponse;
import com.example.investigation_case_service.dto.response.UserResponse;
import com.example.investigation_case_service.exception.ApiException;
import com.example.investigation_case_service.model.enums.reviews.ReviewStage;
import com.example.investigation_case_service.model.enums.reviews.ReviewStatus;
import com.example.investigation_case_service.model.enums.users.Role;
import com.example.investigation_case_service.model.review.RegionChangeReview;
import com.example.investigation_case_service.model.review.RegistrationReviewsDetails;
import com.example.investigation_case_service.model.review.Review;
import com.example.investigation_case_service.model.users.User;
import com.example.investigation_case_service.repository.review.RegionChangeRepository;
import com.example.investigation_case_service.repository.UserRepository;
import com.example.investigation_case_service.repository.review.RegistrationReviewsRepository;
import com.example.investigation_case_service.security.AppUserPrincipal;
import com.example.investigation_case_service.service.RegAdminService;
import com.example.investigation_case_service.util.Pages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public
class ReqAdminImpl implements RegAdminService {
    private final UserRepository userRepository;
    private final RegistrationReviewsRepository registrationReviewsRepository;
    private final RegionChangeRepository regionChangeRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByMyRegion(Long regionId, int page, int size) {
        return userRepository.findAllByRegion(regionId, Pages.of(page, size)).map(UserResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistrationRequestResponse> getRegistrations(Long regionId, ReviewStatus status, int page, int size) {
        return registrationReviewsRepository.findAllByRegionAndStatus(regionId, status, Pages.of(page, size))
                .map(RegistrationRequestResponse::map);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegionChangeResponse> getRegionChanges(Long regionId, ReviewStatus status, int page, int size) {
        return regionChangeRepository.findAllForRegion(regionId, status, Pages.of(page, size))
                .map(RegionChangeResponse::from);
    }


    @Override
    @Transactional
    public String approveRegistration(Long requestId, AppUserPrincipal admin) {
        RegistrationReviewsDetails userReview = pendingRequest(requestId, admin);

        if (userRepository.existsByLogin(userReview.getLogin())) {
            throw ApiException.conflict("Пользователь с логином " + userReview.getLogin() + " уже существует");
        }
        if (userRepository.existsByIin(userReview.getIin())) {
            throw ApiException.conflict("Пользователь с таким ИИН уже существует");
        }
        User u = new User();
        u.setLogin(userReview.getLogin());
        u.setPasswordHash(userReview.getPasswordHash());
        u.setFullName(fullName(userReview));
        u.setRole(Role.USER);
        u.setRegion(userReview.getRegion());
        u.setIin(userReview.getIin());
        u.setFirstName(userReview.getFirstName());
        u.setLastName(userReview.getLastName());
        u.setFatherName(userReview.getFatherName());
        userRepository.saveAndFlush(u);

        close(userReview, ReviewStatus.APPROVED, admin);
        log.info("Registration request [{}] approved by [{}], user [{}] created", userReview.getId(), admin.id(), u.getId());
        return "Запрос на регистрацию пользователя одобрен";
    }

    @Override
    @Transactional
    public RegistrationRequestResponse rejectRegistration(Long requestId, AppUserPrincipal admin) {
        RegistrationReviewsDetails userReview = pendingRequest(requestId, admin);
        close(userReview, ReviewStatus.REJECTED, admin);
        log.info("Registration request [{}] rejected by [{}]", userReview.getId(), admin.id());
        return RegistrationRequestResponse.map(userReview);
    }


    @Override
    @Transactional
    public String approveChangeRegion(Long requestId, AppUserPrincipal admin) {
        RegionChangeReview review = getRegionChangeReview(requestId);
        User adminUser = getUser(admin.id());
        ReviewStage stage = getReviewStage(review, adminUser);
        Instant now = Instant.now();

        if (stage == ReviewStage.DEPARTURE) {
            review.setDepartureStatus(ReviewStatus.APPROVED);
            review.setReviewedBy(adminUser);
            review.setReviewedAt(now);
            review.setArrivalStatus(ReviewStatus.PENDING);
            log.info("Region change request [{}]: departure approved by [{}]", review.getId(), admin.id());
            return "Уход из региона одобрен. Заявка передана администратору нового региона";
        }

        User user = review.getCreatedBy();
        if (!Objects.equals(user.getRegion().getId(), review.getRegionFrom().getId())) {
            throw ApiException.conflict("Пользователь больше не состоит в исходном регионе, заявку нужно отклонить");
        }
        review.setArrivalStatus(ReviewStatus.APPROVED);
        review.setArrivalReviewedBy(adminUser);
        review.setArrivalReviewedAt(now);
        user.setRegion(review.getRegionTo());
        closeReview(review.getReview(), ReviewStatus.APPROVED, adminUser, now);
        log.info("Region change request [{}] approved by [{}], user [{}] moved to region [{}]",
                review.getId(), admin.id(), user.getId(), review.getRegionTo().getId());
        return "Заявка на смену региона одобрена, регион пользователя изменён";
    }

    @Override
    @Transactional
    public String rejectRegionChange(Long requestId, AppUserPrincipal admin) {
        RegionChangeReview review = getRegionChangeReview(requestId);
        User adminUser = getUser(admin.id());
        ReviewStage stage = getReviewStage(review, adminUser);
        Instant now = Instant.now();

        if (stage == ReviewStage.DEPARTURE) {
            review.setDepartureStatus(ReviewStatus.REJECTED);
            review.setReviewedBy(adminUser);
            review.setReviewedAt(now);
        } else {
            review.setArrivalStatus(ReviewStatus.REJECTED);
            review.setArrivalReviewedBy(adminUser);
            review.setArrivalReviewedAt(now);
        }
        closeReview(review.getReview(), ReviewStatus.REJECTED, adminUser, now);
        log.info("Region change request [{}] rejected at stage [{}] by [{}]", review.getId(), stage, admin.id());
        return "Заявка на смену региона отклонена";
    }

    private RegistrationReviewsDetails pendingRequest(Long requestId, AppUserPrincipal admin) {
        RegistrationReviewsDetails userReview = registrationReviewsRepository.findByIdAndRegion(requestId, admin.regionId())
                .orElseThrow(() -> ApiException.notFound("Заявка не найдена"));
        if (userReview.getReviewStatus() != ReviewStatus.PENDING) {
            throw ApiException.conflict("Заявка уже рассмотрена: " + userReview.getReviewStatus());
        }
        return userReview;
    }

    private void close(RegistrationReviewsDetails userReview, ReviewStatus status, AppUserPrincipal admin) {
        User adminUser = userRepository.getReferenceById(admin.id());
        Instant now = Instant.now();
        userReview.setReviewStatus(status);
        userReview.setReviewedBy(adminUser);
        userReview.setReviewedAt(now);
        userReview.setPasswordHash(null);
        closeReview(userReview.getReview(), status, adminUser, now);
    }

    private void closeReview(Review review, ReviewStatus status, User adminUser, Instant now) {
        review.setReviewStatus(status);
        review.setReviewedBy(adminUser);
        review.setReviewedAt(now);
    }

    private RegionChangeReview getRegionChangeReview(Long requestId) {
        return regionChangeRepository.findById(requestId)
                .orElseThrow(() -> ApiException.notFound("Заявка не найдена"));
    }

    private User getUser(Long userId) {
        return userRepository.findWithRegionById(userId)
                .orElseThrow(() -> ApiException.notFound("Пользователь не найден"));
    }

    private ReviewStage getReviewStage(RegionChangeReview review, User adminUser) {
        Long adminRegionId = adminUser.getRegion().getId();
        boolean departureAdmin = Objects.equals(review.getRegionFrom().getId(), adminRegionId);
        boolean arrivalAdmin = Objects.equals(review.getRegionTo().getId(), adminRegionId);
        if (!departureAdmin && !arrivalAdmin) {
            throw ApiException.forbidden("У вас нет доступа к рассмотрению этой заявки");
        }
        if (review.getReview().getReviewStatus() != ReviewStatus.PENDING) {
            throw ApiException.conflict("Заявка уже рассмотрена: " + review.getReview().getReviewStatus());
        }
        if (departureAdmin) {
            if (review.getDepartureStatus() != ReviewStatus.PENDING) {
                throw ApiException.conflict("Вы уже рассмотрели эту заявку, она ожидает администратора нового региона");
            }
            return ReviewStage.DEPARTURE;
        }
        if (review.getDepartureStatus() != ReviewStatus.APPROVED) {
            throw ApiException.conflict("Сначала необходимо одобрить уход из текущего региона");
        }
        if (review.getArrivalStatus() != ReviewStatus.PENDING) {
            throw ApiException.conflict("Заявка уже рассмотрена");
        }
        return ReviewStage.ARRIVAL;
    }

    private static String fullName(RegistrationReviewsDetails userReview) {
        String name = userReview.getLastName() + " " + userReview.getFirstName();
        return userReview.getFatherName() == null || userReview.getFatherName().isBlank()
                ? name : name + " " + userReview.getFatherName();
    }
}
