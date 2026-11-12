package com.example.investigation_case_service.model.cases;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "case_files")
@Getter @Setter @NoArgsConstructor
public class CaseFileRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id")
    private Case caseEntity;
}
