package com.erp.montfortuganda.fees.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record FeeStructureAssignmentResponse(
        Long feeStructureId,
        int studentsAssigned,
        int feeItemsAssigned,
        BigDecimal totalAssigned,
        List<StudentAssignmentResult> students
) {
    public record StudentAssignmentResult(
            Long studentId,
            String admissionNo,
            String studentName,
            int feeItemsAssigned,
            BigDecimal totalAssigned,
            List<Long> feeAssignmentIds
    ) { }
}
