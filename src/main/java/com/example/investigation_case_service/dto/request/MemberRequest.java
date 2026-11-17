package com.example.investigation_case_service.dto.request;

import com.example.investigation_case_service.model.enums.users.MemberType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberRequest {
    @NotNull
    private MemberType memberType;

    private Boolean canView;

    private Boolean canEdit;

    private List<Long> fileIds;
}
