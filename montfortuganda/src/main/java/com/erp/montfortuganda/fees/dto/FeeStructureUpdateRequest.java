package com.erp.montfortuganda.fees.dto;
import com.erp.montfortuganda.fees.entity.ErpFeeStructure.ChargingFrequency;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record FeeStructureUpdateRequest(
 @NotNull @Positive Long academicYearId,
 @NotNull @Positive Integer levelId,
 @NotNull @Positive Integer startClassId,
 @Positive Integer endClassId,
 @NotBlank @Size(max=150) String feeName,
 @NotBlank @Size(max=50) String feeType,
 @NotBlank @Size(max=50) String feeCode,
 @NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal amount,
 @NotNull ChargingFrequency chargingFrequency,
 @Size(max=500) String remarks) {}
