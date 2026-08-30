package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ScholarshipMultipleDonorAllocationRequestDTO {

    private Long branchId;

    /**
     * Exact Scholarship History/cycle receiving the funding.
     */
    private Long scholarshipHistoryId;

    /**
     * Student receiving the combined donor funding.
     */
    private Long studentId;

    /**
     * Each item represents one donor allocation and will be persisted
     * as a separate Scholarship allocation transaction.
     */
    private List<DonorAllocationItem> allocations;

    @Data
    public static class DonorAllocationItem {

        private Long donationId;

        private BigDecimal amountUgx;

        /**
         * Term covered by this donor allocation.
         */
        private String term;

        /**
         * Academic year covered by this donor allocation.
         */
        private String academicYear;
    }
}
