package com.example.investigation_case_service.model.cases;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
@Embeddable
@Getter
@Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class CaseMemberId implements Serializable {
    @Column(name = "case_id")
    private Long caseId;
    @Column(name = "user_id")
    private Long userId;
}
