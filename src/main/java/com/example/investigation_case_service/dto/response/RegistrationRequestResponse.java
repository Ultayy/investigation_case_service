package com.example.investigation_case_service.dto.response;

import com.example.investigation_case_service.model.enums.reviews.ReviewStatus;
import com.example.investigation_case_service.model.review.RegistrationReviewsDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequestResponse {
    private Long id;
    private String email;
    private String iin;
    private String name;
    private String surname;
    private String fatherName;
    private Long regionId;
    private String regionCode;
    private ReviewStatus status;
    private Instant createdAt;
    private Instant reviewedAt;

    public static RegistrationRequestResponse map(RegistrationReviewsDetails userReview) {
        return new RegistrationRequestResponse(userReview.getId(), userReview.getLogin(), userReview.getIin(), userReview.getFirstName(), userReview.getLastName(), userReview.getFatherName(), userReview.getRegion().getId(),
                userReview.getRegion().getCode(), userReview.getReviewStatus(), userReview.getCreatedAt(), userReview.getReviewedAt());
    }
}
