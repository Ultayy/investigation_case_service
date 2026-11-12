package com.example.investigation_case_service.repository;

import com.example.investigation_case_service.model.cases.FileAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileAccessRepository extends JpaRepository<FileAccess, Long> {

    @Query("select a from FileAccess a join fetch a.file f where a.user.id = :userId and f.caseEntity.id = :caseId")
    List<FileAccess> findAllByUserAndCase(@Param("userId") Long userId, @Param("caseId") Long caseId);
}
