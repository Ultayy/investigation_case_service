package com.example.investigation_case_service.dto.response;

import com.example.investigation_case_service.model.enums.reviews.ReviewStatus;
import com.example.investigation_case_service.model.review.RegionChangeReview;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegionChangeResponse {
    private Long id;
    private Long userId;
    private String userLogin;
    private String userFullName;
    private Long regionFromId;
    private String regionFromCode;
    private Long regionToId;
    private String regionToCode;
    private ReviewStatus departureStatus;
    private ReviewStatus arrivalStatus;
    private ReviewStatus status;
    private Instant createdAt;

    public static RegionChangeResponse from(RegionChangeReview r) {
        return new RegionChangeResponse(r.getId(),
                r.getCreatedBy().getId(), r.getCreatedBy().getLogin(), r.getCreatedBy().getFullName(),
                r.getRegionFrom().getId(), r.getRegionFrom().getCode(),
                r.getRegionTo().getId(), r.getRegionTo().getCode(),
                r.getDepartureStatus(), r.getArrivalStatus(),
                r.getReview().getReviewStatus(), r.getReview().getCreatedAt());
    }
}
