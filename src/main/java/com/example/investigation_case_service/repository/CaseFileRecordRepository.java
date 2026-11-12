package com.example.investigation_case_service.repository;

import com.example.investigation_case_service.model.cases.CaseFileRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseFileRecordRepository extends JpaRepository<CaseFileRecord, Long> {

    @Query("select f from CaseFileRecord f where f.caseEntity.id = :caseId order by f.id")
    List<CaseFileRecord> findAllByCase(@Param("caseId") Long caseId);

    @Query("""
            select f from CaseFileRecord f
            where f.caseEntity.id = :caseId
              and exists (select 1 from FileAccess a where a.file.id = f.id and a.user.id = :userId)
            order by f.id
            """)
    List<CaseFileRecord> findAllowedByCaseAndUser(@Param("caseId") Long caseId, @Param("userId") Long userId);
}
