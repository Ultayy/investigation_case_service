package com.example.investigation_case_service.repository;

import com.example.investigation_case_service.model.cases.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    @Query("select e from AuditEvent e join fetch e.actor where e.caseId = :caseId order by e.createdAt, e.id")
    List<AuditEvent> findAllByCase(@Param("caseId") Long caseId);
}
