package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DonorDTO {
    private Long id;
    private String receiptNumber;
    private String fullName;
    private String email;
    private String currency;
    private BigDecimal amount;
    private BigDecimal amountReceivedUgx;
    private BigDecimal amountSpentUgx;
    private Integer studentsBenefited;
    private String term;
}