package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AllocationRequestDTO {
    private Long branchId;
    private Long studentId;
    private Long donationId;
    private BigDecimal amountUgx;
    private String term;
    private String academicYear;
}
