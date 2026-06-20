package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BranchDemandDTO {
    private Long branchId;
    private String branchName;
    private Integer totalPendingRequests;
    private BigDecimal totalDeficitUgx;
}