package com.example.investigation_case_service.repository;

import com.example.investigation_case_service.model.cases.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {

    boolean existsByNumber(String number);

    @Query("select c from InvestigationCase c join fetch c.owner join fetch c.region where c.id = :id")
    Optional<Case> findDetailedById(@Param("id") Long id);

    @Query(value = """
            select c from InvestigationCase c join fetch c.owner join fetch c.region
            where c.owner.id = :userId
               or exists (select 1 from CaseMember m
                          where m.id.caseId = c.id and m.id.userId = :userId and m.canView = true)
            order by c.createdAt desc, c.id desc
            """,
            countQuery = """
            select count(c) from InvestigationCase c
            where c.owner.id = :userId
               or exists (select 1 from CaseMember m
                          where m.id.caseId = c.id and m.id.userId = :userId and m.canView = true)
            """)
    Page<Case> findAccessibleTo(@Param("userId") Long userId, Pageable pageable);

    @Query(value = """
            select c from InvestigationCase c join fetch c.owner join fetch c.region r
            where r.id = :regionId order by c.createdAt desc, c.id desc
            """,
            countQuery = "select count(c) from InvestigationCase c where c.region.id = :regionId")
    Page<Case> findAllByRegion(@Param("regionId") Long regionId, Pageable pageable);

    @Query(value = "select c from InvestigationCase c join fetch c.owner join fetch c.region order by c.createdAt desc, c.id desc",
            countQuery = "select count(c) from InvestigationCase c")
    Page<Case> findAllDetailed(Pageable pageable);
}
