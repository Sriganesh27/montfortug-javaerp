package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ScholarshipBulkAllocationRequestDTO {

    private Long branchId;

    /**
     * Exact Scholarship History/cycle receiving these allocations.
     */
    private Long scholarshipHistoryId;

    /**
     * Donor whose funds are being distributed to the students.
     */
    private Long donationId;

    /**
     * Each item creates one separate Scholarship allocation transaction.
     */
    private List<StudentAllocationItem> allocations;

    @Data
    public static class StudentAllocationItem {

        private Long studentId;

        private BigDecimal amountUgx;

        /**
         * Term covered by this individual student allocation.
         */
        private String term;

        /**
         * Academic year covered by this individual student allocation.
         */
        private String academicYear;
    }
}
