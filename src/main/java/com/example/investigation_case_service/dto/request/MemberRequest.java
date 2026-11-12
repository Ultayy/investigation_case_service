package com.example.investigation_case_service.dto.request;

import com.example.investigation_case_service.model.enums.MemberType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.NonNull;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberRequest {
    @NonNull
    private MemberType memberType;

    private Boolean canView;

    private Boolean canEdit;

    private List<Long> fileIds;
}
