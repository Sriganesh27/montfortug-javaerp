package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ScholarshipBranchDemandDTO {
    private Integer branchId;
    private String branchName;
    private Long totalApplicants;
    private BigDecimal totalRequestedAmountUgx;
}