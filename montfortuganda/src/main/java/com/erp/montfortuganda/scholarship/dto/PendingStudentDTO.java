package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PendingStudentDTO {
    private Long studentId;
    private String studentName;
    private String campusName;
    private Long campusId;
    private String currentClass;
    private BigDecimal shortfallUgx;
    private BigDecimal totalFeesUgx;
}