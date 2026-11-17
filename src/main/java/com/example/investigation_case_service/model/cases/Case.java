package com.example.investigation_case_service.model.cases;

import com.example.investigation_case_service.model.enums.cases.CaseStatus;
import com.example.investigation_case_service.model.users.Region;
import com.example.investigation_case_service.model.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity(name = "InvestigationCase")
@Table(name = "cases")
@Getter
@Setter @NoArgsConstructor
public class Case {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String number;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CaseStatus status = CaseStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id")
    private Region region;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}