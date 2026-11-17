package com.example.investigation_case_service.repository.review;

import com.example.investigation_case_service.model.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

}
