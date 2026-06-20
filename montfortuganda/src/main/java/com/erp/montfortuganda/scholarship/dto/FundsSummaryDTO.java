package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FundsSummaryDTO {
    private BigDecimal totalRaisedUgx;
    private BigDecimal totalSpentUgx;
    private BigDecimal availableBalanceUgx;
    private Integer studentsSponsored;
}