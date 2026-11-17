package com.example.investigation_case_service.service.impl;

import com.example.investigation_case_service.exception.ApiException;
import com.example.investigation_case_service.model.enums.reviews.ReviewStatus;
import com.example.investigation_case_service.model.enums.reviews.ReviewType;
import com.example.investigation_case_service.model.review.RegionChangeReview;
import com.example.investigation_case_service.model.review.Review;
import com.example.investigation_case_service.model.users.Region;
import com.example.investigation_case_service.model.users.User;
import com.example.investigation_case_service.repository.review.RegionChangeRepository;
import com.example.investigation_case_service.repository.RegionRepository;
import com.example.investigation_case_service.repository.UserRepository;
import com.example.investigation_case_service.repository.review.ReviewRepository;
import com.example.investigation_case_service.security.AppUserPrincipal;
import com.example.investigation_case_service.service.InvestigatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestigatorServiceImpl implements InvestigatorService {
    private final RegionRepository regionRepository;
    private final RegionChangeRepository regionChangeRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public String changeRegion(Long toRegionId, AppUserPrincipal userPrincipal) {
        User user = getUser(userPrincipal);
        Region regionFrom = user.getRegion();
        Region regionTo = getRegion(toRegionId);
        if (regionFrom.getId().equals(regionTo.getId())) {
            throw ApiException.conflict("Вы уже состоите в этом регионе");
        }
        if (regionChangeRepository.existsByCreatedBy_IdAndReview_ReviewStatus(user.getId(), ReviewStatus.PENDING)) {
            throw ApiException.conflict("У вас уже есть нерассмотренная заявка на смену региона");
        }
        RegionChangeReview userReview = new RegionChangeReview();
        userReview.setCreatedBy(user);
        userReview.setRegionFrom(regionFrom);
        userReview.setRegionTo(regionTo);
        userReview.setDepartureStatus(ReviewStatus.PENDING);
        userReview.setArrivalStatus(ReviewStatus.PENDING_CONFIRM);
        userReview.setReview(createReview(ReviewType.CHANGE_REGION));

        regionChangeRepository.save(userReview);
        log.info("Region change request [{}] created by user [{}]: [{}] -> [{}]",
                userReview.getId(), user.getId(), regionFrom.getId(), regionTo.getId());

        return "Заявка на смену региона отправлена региональному администратору";
    }

    private Review createReview(ReviewType reviewType) {
        Review review = new Review();
        review.setReviewStatus(ReviewStatus.PENDING);
        review.setReviewType(reviewType);
        return reviewRepository.save(review);
    }

    private Region getRegion(Long id) {
        return regionRepository.findById(id).orElseThrow(() -> ApiException.badRequest("Регион " + id + " не найден"));
    }

    private User getUser(AppUserPrincipal userPrincipal) {
        return userRepository.findWithRegionById(userPrincipal.id())
                .orElseThrow(() -> ApiException.notFound("Пользователь " + userPrincipal.id() + " не найден"));
    }
}
