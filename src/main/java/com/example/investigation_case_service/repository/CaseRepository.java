package com.example.investigation_case_service.repository;

import com.example.investigation_case_service.model.cases.Case;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {

    boolean existsByNumber(String number);

    @Query("select c from InvestigationCase c join fetch c.owner join fetch c.region where c.id = :id")
    Optional<Case> findDetailedById(@Param("id") Long id);

    @Query("""
            select c from InvestigationCase c join fetch c.owner join fetch c.region
            where c.owner.id = :userId
               or exists (select 1 from CaseMember m
                          where m.id.caseId = c.id and m.id.userId = :userId and m.canView = true)
            order by c.createdAt desc
            """)
    List<Case> findAccessibleTo(@Param("userId") Long userId);

    @Query("""
            select c from InvestigationCase c join fetch c.owner join fetch c.region r
            where r.id = :regionId order by c.createdAt desc
            """)
    List<Case> findAllByRegion(@Param("regionId") Long regionId);

    @Query("select c from InvestigationCase c join fetch c.owner join fetch c.region order by c.createdAt desc")
    List<Case> findAllDetailed();
}
