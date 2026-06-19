package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ScholarshipPendingStudentDTO {
    private Integer studentId;
    private String studentName;
    private String campus;
    private String currentClass;
    private BigDecimal feesUgx;
    private BigDecimal shortfallUgx;
    private String hardshipReason;
    private BigDecimal academicScore;
}
