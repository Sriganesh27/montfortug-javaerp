package com.erp.montfortuganda.school.dto;

import com.erp.montfortuganda.school.entity.ErpSubject;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubjectRequestDTO(

        @NotBlank
        @Size(max = 20)
        String subjectCode,

        @NotBlank
        @Size(max = 100)
        String subjectName,

        @Size(max = 50)
        String subjectShortName,

        @NotNull
        ErpSubject.SubjectType subjectType,

        @NotNull
        Boolean practical,

        @NotNull
        @Min(1)
        Integer displayOrder,

        @Size(max = 500)
        String description,

        @NotNull
        ErpSubject.Status status
) {
}
