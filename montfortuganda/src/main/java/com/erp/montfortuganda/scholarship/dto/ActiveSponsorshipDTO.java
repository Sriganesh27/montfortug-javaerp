package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ActiveSponsorshipDTO {
    private Long pairingId;
    private String studentName;
    private String branchName;
    private String donorName;
    private BigDecimal amountCoveredUgx;
    private String matchDate;
}