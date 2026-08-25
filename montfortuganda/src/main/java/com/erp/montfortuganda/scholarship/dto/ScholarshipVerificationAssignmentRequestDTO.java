package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;

@Data
public class ScholarshipVerificationAssignmentRequestDTO {
    private Long employeeId;
    private Boolean useEntranceTestEmployee;

    public boolean isAssignmentChoiceValid() {
        return employeeId != null || Boolean.TRUE.equals(useEntranceTestEmployee);
    }
}
