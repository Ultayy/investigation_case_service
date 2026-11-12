package com.example.investigation_case_service.repository;

import com.example.investigation_case_service.model.users.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
}
