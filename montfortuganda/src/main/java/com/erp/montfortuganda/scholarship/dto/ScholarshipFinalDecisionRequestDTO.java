package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScholarshipFinalDecisionRequestDTO {
    private String decision;
    private BigDecimal approvedAmount;
    private BigDecimal approvedPercentage;
    private String remarks;
}
