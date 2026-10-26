package com.erp.montfortuganda.fees.dto;

import com.erp.montfortuganda.fees.entity.ErpFeeStructureItem.ChargingFrequency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeStructureItemRequest(
        @NotBlank @Size(max = 50) String feeType,
        @NotBlank @Size(max = 150) String feeName,
        @NotNull @DecimalMin(value = "0.00") BigDecimal amount,
        @NotNull ChargingFrequency chargingFrequency,
        LocalDate dueDate,
        @NotNull Boolean mandatory,
        @NotNull @Min(0) Integer displayOrder,
        @Size(max = 500) String remarks
) {
}
