package com.erp.montfortuganda.fees.dto;

import com.erp.montfortuganda.fees.entity.ErpFeeStructureItem.ChargingFrequency;
import com.erp.montfortuganda.fees.entity.ErpFeeStructureItem.StructureStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FeeStructureItemRequest(
        @NotBlank @Size(max = 50) String feeType,
        @NotBlank @Size(max = 50) String feeCode,
        @NotBlank @Size(max = 150) String feeName,
        @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal amount,
        @NotNull ChargingFrequency chargingFrequency,
        @NotNull StructureStatus structureStatus,
        @Size(max = 500) String remarks
) {
}
