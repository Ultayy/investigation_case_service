package com.example.investigation_case_service.repository.review;

import com.example.investigation_case_service.model.enums.reviews.ReviewStatus;
import com.example.investigation_case_service.model.review.RegistrationReviewsDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegistrationReviewsRepository extends JpaRepository<RegistrationReviewsDetails, Long> {

    boolean existsByLoginAndReviewStatus(String login, ReviewStatus reviewStatus);

    boolean existsByIinAndReviewStatus(String iin, ReviewStatus reviewStatus);

    @Query(value = """
            select r from RegistrationReviewsDetails r join fetch r.region reg
            where reg.id = :regionId and r.reviewStatus = :status
            order by r.createdAt, r.id
            """,
            countQuery = """
            select count(r)
            from RegistrationReviewsDetails r
            where r.region.id = :regionId and r.reviewStatus = :status
            """)
    Page<RegistrationReviewsDetails> findAllByRegionAndStatus(@Param("regionId") Long regionId,
                                                              @Param("status") ReviewStatus status,
                                                              Pageable pageable);

    @Query("""
        select r
        from RegistrationReviewsDetails r
        join r.region reg
        join r.review rev
        where r.id = :id
          and reg.id = :regionId
        """)
    Optional<RegistrationReviewsDetails> findByIdAndRegion(@Param("id") Long id, @Param("regionId") Long regionId);
}
