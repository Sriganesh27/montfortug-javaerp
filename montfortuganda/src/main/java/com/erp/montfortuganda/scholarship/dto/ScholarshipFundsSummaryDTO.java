package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ScholarshipFundsSummaryDTO {
    private BigDecimal totalRaisedUgx;
    private BigDecimal totalSpentUgx;
    private BigDecimal availableBalanceUgx;
    private Integer studentsSponsored;
}