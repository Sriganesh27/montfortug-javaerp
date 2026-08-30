package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;

@Data
public class ScholarshipBranchDistributionCompletionRequestDTO {

    /**
     * Exact Scholarship History/cycle for which Branch Admin confirms
     * the distribution outcome.
     */
    private Long scholarshipHistoryId;

    /**
     * Confirmation note retained with the distribution action.
     */
    private String remarks;
}
