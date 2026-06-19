package com.erp.montfortuganda.scholarship.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ScholarshipDonorDTO {
    private Integer id;
    private String receiptNumber;
    private String fullName;
    private String email;
    private String currency;
    private BigDecimal amountReceivedUgx;
    private BigDecimal amountSpentUgx;
}
