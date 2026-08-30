package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AllocationRequestDTO {
    private Long branchId;

    /**
     * Identifies the exact Scholarship History/cycle being funded.
     * This allows one Scholarship Application to retain multiple
     * year/term allocation histories without mixing financial records.
     */
    private Long scholarshipHistoryId;
    private Long studentId;
    private Long donationId;
    private BigDecimal amountUgx;
    private String term;
    private String academicYear;
}
