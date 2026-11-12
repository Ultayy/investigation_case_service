package com.example.investigation_case_service.model.cases;
import com.example.investigation_case_service.model.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "file_access", uniqueConstraints = @UniqueConstraint(columnNames = {"file_id", "user_id"}))
@Getter @Setter @NoArgsConstructor
public class FileAccess {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id")
    private CaseFileRecord file;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt = Instant.now();
}
