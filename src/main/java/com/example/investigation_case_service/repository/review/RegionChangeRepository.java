package com.example.investigation_case_service.repository.review;

import com.example.investigation_case_service.model.enums.reviews.ReviewStatus;
import com.example.investigation_case_service.model.review.RegionChangeReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionChangeRepository extends JpaRepository<RegionChangeReview, Long> {

    boolean existsByCreatedBy_IdAndReview_ReviewStatus(Long userId, ReviewStatus status);

    @Query(value = """
            select r from RegionChangeReview r
            join fetch r.createdBy join fetch r.regionFrom join fetch r.regionTo join fetch r.review rv
            where (r.regionFrom.id = :regionId or r.regionTo.id = :regionId) and rv.reviewStatus = :status
            order by r.id desc
            """,
            countQuery = """
            select count(r) from RegionChangeReview r
            where (r.regionFrom.id = :regionId or r.regionTo.id = :regionId) and r.review.reviewStatus = :status
            """)
    Page<RegionChangeReview> findAllForRegion(@Param("regionId") Long regionId,
                                              @Param("status") ReviewStatus status,
                                              Pageable pageable);
}
