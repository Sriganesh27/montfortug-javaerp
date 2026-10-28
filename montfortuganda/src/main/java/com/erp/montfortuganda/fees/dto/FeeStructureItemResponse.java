package com.erp.montfortuganda.fees.dto;

import com.erp.montfortuganda.fees.entity.ErpFeeStructureItem.ChargingFrequency;
import com.erp.montfortuganda.fees.entity.ErpFeeStructureItem.StructureStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FeeStructureItemResponse(
        Long feeStructureItemId,
        Integer branchId,
        String feeType,
        String feeCode,
        String feeName,
        BigDecimal amount,
        ChargingFrequency chargingFrequency,
        StructureStatus structureStatus,
        Boolean active,
        String remarks,
        Long rowVersion,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
