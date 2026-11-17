package com.example.investigation_case_service.model.cases;

import com.example.investigation_case_service.model.enums.users.MemberType;
import com.example.investigation_case_service.model.users.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "case_members")
@Getter
@Setter
@NoArgsConstructor
public class CaseMember {
    @EmbeddedId
    private CaseMemberId id;

    @MapsId("caseId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private Case caseEntity;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false, length = 32)
    private MemberType memberType;

    @Column(name = "can_view", nullable = false)
    private boolean canView;

    @Column(name = "can_edit", nullable = false)
    private boolean canEdit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "granted_by")
    private User grantedBy;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
