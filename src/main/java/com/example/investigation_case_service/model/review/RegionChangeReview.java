package com.example.investigation_case_service.model.review;

import com.example.investigation_case_service.model.enums.reviews.ReviewStatus;
import com.example.investigation_case_service.model.users.Region;
import com.example.investigation_case_service.model.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "region_change_reviews")
public class RegionChangeReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "old_region_id", nullable = false)
    private Region regionFrom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "new_region_id", nullable = false)
    private Region regionTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "departure_review_status", nullable = false, length = 32)
    private ReviewStatus departureStatus = ReviewStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "arrival_status", nullable = false, length = 32)
    private ReviewStatus arrivalStatus = ReviewStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrival_reviewed_by")
    private User arrivalReviewedBy;

    @Column(name = "arrival_reviewed_at")
    private Instant arrivalReviewedAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false, unique = true)
    private Review review;

}
