package com.example.investigation_case_service.repository;

import com.example.investigation_case_service.model.cases.CaseMember;
import com.example.investigation_case_service.model.cases.CaseMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseMemberRepository extends JpaRepository<CaseMember, CaseMemberId> {
}
