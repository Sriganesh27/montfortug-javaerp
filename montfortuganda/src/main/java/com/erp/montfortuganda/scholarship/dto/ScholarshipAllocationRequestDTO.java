package com.erp.montfortuganda.scholarship.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ScholarshipAllocationRequestDTO {
    @NotNull
    private Integer branchId;
    private Integer studentId;
    private Integer donationId;
    @NotNull
    private BigDecimal amountUgx;
    @NotNull
    private String term;
    @NotNull
    private String academicYear;
}