package com.erp.montfortuganda.employee.dto.response;

import com.erp.montfortuganda.employee.enums.EmployeeCreationStage;

import java.time.LocalDateTime;

public record EmployeeCreationErrorResponse(

        boolean success,

        EmployeeCreationStage stage,

        String errorCode,

        String message,

        String field,

        LocalDateTime timestamp

) {

    public static EmployeeCreationErrorResponse of(
            EmployeeCreationStage stage,
            String errorCode,
            String message,
            String field
    ) {
        return new EmployeeCreationErrorResponse(
                false,
                stage,
                errorCode,
                message,
                field,
                LocalDateTime.now()
        );
    }
}